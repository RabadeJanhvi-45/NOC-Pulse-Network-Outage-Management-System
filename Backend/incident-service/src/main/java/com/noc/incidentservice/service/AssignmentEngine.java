package com.noc.incidentservice.service;

import com.noc.incidentservice.client.AuthServiceClient;
import com.noc.incidentservice.client.DeviceServiceClient;
import com.noc.incidentservice.client.NotificationServiceClient;
import com.noc.incidentservice.dto.CreateNotificationRequest;
import com.noc.incidentservice.dto.DeviceTypeSpecializationResponse;
import com.noc.incidentservice.dto.EngineerResponse;
import com.noc.incidentservice.entity.Incident;
import com.noc.incidentservice.repository.AssignmentRepository;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Ranked Engineer-matching per spec §3:
 *   1. Primary specialization match
 *   2. Secondary skill match
 *   3. Active-task limit not exceeded
 *   4. Fewest active tasks among matches
 *   5. Round-robin for ties
 *   6. No specialization/skill match -> fallback to fewest-active-tasks Engineer, round-robin for ties
 *   7. No active Engineer at all -> caller leaves the incident unassigned; Admin is notified here.
 *
 * "Active tasks" = incidents in ASSIGNED or IN_PROGRESS only.
 *
 * Round-robin state is kept in memory only (per matched-group key) — it
 * resets on service restart. That's an accepted simplification: it still
 * spreads load evenly during normal operation, it just doesn't survive a
 * redeploy, same class of limitation already documented for other dev-only
 * shortcuts in this project (see README "Known gaps").
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AssignmentEngine {

    private static final List<String> ACTIVE_STATUSES = List.of("ASSIGNED", "IN_PROGRESS");

    private final AuthServiceClient authServiceClient;
    private final DeviceServiceClient deviceServiceClient;
    private final NotificationServiceClient notificationServiceClient;
    private final AssignmentRepository assignmentRepository;

    private final Map<String, AtomicInteger> roundRobinCounters = new ConcurrentHashMap<>();

    /** Convenience overload: no Engineers excluded from consideration. */
    public Optional<String> findEngineerForIncident(Incident incident) {
        return findEngineerForIncident(incident, Set.of());
    }

    /**
     * Finds the best Engineer for this incident's device type, per the
     * ranked rules above. {@code excludedEngineerIds} is removed from the
     * candidate pool before ranking — used on reassignment after Admin
     * approves an Engineer's rejection, so that Engineer isn't handed the
     * same incident straight back.
     *
     * Returns empty if no eligible Engineer exists at all (everyone
     * disabled, excluded, or at their active-task limit) — Admin is
     * notified here so every caller (sync create + retry scanner +
     * reassignment-after-rejection) gets the same behavior for free.
     */
    public Optional<String> findEngineerForIncident(Incident incident, Set<String> excludedEngineerIds) {
        String requiredSpecialization = resolveRequiredSpecialization(incident.getDeviceId());

        List<EngineerResponse> engineers;
        try {
            engineers = authServiceClient.getEngineers();
        } catch (FeignException ex) {
            log.error("Could not reach auth-service to fetch Engineers for incident {}: {}",
                    incident.getId(), ex.getMessage());
            return Optional.empty();
        }

        List<EngineerResponse> enabled = engineers.stream()
                .filter(e -> Boolean.TRUE.equals(e.getEnabled()))
                .filter(e -> !excludedEngineerIds.contains(String.valueOf(e.getUserId())))
                .toList();

        Map<String, Integer> activeCounts = loadActiveCounts();

        // Rank 1: primary specialization match, under limit
        List<EngineerResponse> pool = filterUnderLimit(enabled.stream()
                .filter(e -> matchesPrimary(e, requiredSpecialization))
                .toList(), activeCounts);
        String groupKey = "primary:" + requiredSpecialization;

        // Rank 2: secondary skill match, under limit
        if (pool.isEmpty()) {
            pool = filterUnderLimit(enabled.stream()
                    .filter(e -> matchesSkill(e, requiredSpecialization))
                    .toList(), activeCounts);
            groupKey = "skill:" + requiredSpecialization;
        }

        // Rank 3: fallback — anyone under limit, regardless of match
        if (pool.isEmpty()) {
            pool = filterUnderLimit(enabled, activeCounts);
            groupKey = "fallback";
        }

        if (pool.isEmpty()) {
            notifyAdminNoEngineerAvailable(incident);
            return Optional.empty();
        }

        String chosen = pickFewestActiveWithRoundRobin(pool, activeCounts, groupKey);
        return Optional.of(chosen);
    }

    // ---- ranking helpers ----

    private boolean matchesPrimary(EngineerResponse e, String requiredSpecialization) {
        return requiredSpecialization != null
                && requiredSpecialization.equalsIgnoreCase(e.getPrimarySpecialization());
    }

    private boolean matchesSkill(EngineerResponse e, String requiredSpecialization) {
        return requiredSpecialization != null
                && e.getSkills() != null
                && e.getSkills().stream().anyMatch(s -> s.equalsIgnoreCase(requiredSpecialization));
    }

    private List<EngineerResponse> filterUnderLimit(List<EngineerResponse> candidates, Map<String, Integer> activeCounts) {
        return candidates.stream()
                .filter(e -> {
                    int limit = e.getActiveTaskLimit() != null ? e.getActiveTaskLimit() : Integer.MAX_VALUE;
                    int current = activeCounts.getOrDefault(String.valueOf(e.getUserId()), 0);
                    return current < limit;
                })
                .toList();
    }

    private String pickFewestActiveWithRoundRobin(List<EngineerResponse> pool, Map<String, Integer> activeCounts, String groupKey) {
        int minActive = pool.stream()
                .mapToInt(e -> activeCounts.getOrDefault(String.valueOf(e.getUserId()), 0))
                .min()
                .orElse(0);

        List<String> tied = pool.stream()
                .filter(e -> activeCounts.getOrDefault(String.valueOf(e.getUserId()), 0) == minActive)
                .map(e -> String.valueOf(e.getUserId()))
                .sorted()
                .collect(Collectors.toList());

        if (tied.size() == 1) {
            return tied.get(0);
        }

        AtomicInteger counter = roundRobinCounters.computeIfAbsent(groupKey, k -> new AtomicInteger(0));
        int index = Math.floorMod(counter.getAndIncrement(), tied.size());
        return tied.get(index);
    }

    private Map<String, Integer> loadActiveCounts() {
        Map<String, Integer> counts = new HashMap<>();
        for (Object[] row : assignmentRepository.countActiveByAssignee(ACTIVE_STATUSES)) {
            String assigneeId = (String) row[0];
            long count = (Long) row[1];
            counts.put(assigneeId, (int) count);
        }
        return counts;
    }

    /** Looks up the device's type, then the specialization that type requires. Null if either lookup fails/is unmapped. */
    private String resolveRequiredSpecialization(String deviceId) {
        try {
            var device = deviceServiceClient.getDeviceByBusinessId(deviceId);
            if (device == null || device.getDeviceType() == null) {
                return null;
            }
            DeviceTypeSpecializationResponse mapping = deviceServiceClient.getRequiredSpecialization(device.getDeviceType());
            return mapping != null ? mapping.getRequiredSpecialization() : null;
        } catch (FeignException.NotFound ex) {
            return null; // no mapping configured for this device type — fine, falls through to fallback ranking
        } catch (FeignException ex) {
            log.warn("Could not resolve required specialization for device {}: {}", deviceId, ex.getMessage());
            return null;
        }
    }

    private void notifyAdminNoEngineerAvailable(Incident incident) {
        try {
            notificationServiceClient.create(CreateNotificationRequest.builder()
                    .role("ADMIN")
                    .type("NO_ENGINEER_AVAILABLE")
                    .message("No Engineer available to assign incident #" + incident.getId()
                            + " (device " + incident.getDeviceId() + ", priority " + incident.getPriority() + ")")
                    .build());
        } catch (Exception ex) {
            log.error("Failed to notify Admin about unassigned incident {}: {}", incident.getId(), ex.getMessage());
        }
    }
}