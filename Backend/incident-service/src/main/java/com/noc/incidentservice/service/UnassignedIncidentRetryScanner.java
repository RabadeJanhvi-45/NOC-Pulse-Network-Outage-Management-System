package com.noc.incidentservice.service;

import com.noc.incidentservice.entity.Assignment;
import com.noc.incidentservice.entity.Incident;
import com.noc.incidentservice.repository.AssignmentRepository;
import com.noc.incidentservice.repository.IncidentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Scheduled job (spec §3 rule 7 / §5.4-B): periodically re-attempts
 * assignment for any incident still stuck at NEW (unassigned) — e.g.
 * because no Engineer was available at creation time and one has since
 * become free. Mirrors SlaBreachScanner's existing pattern.
 *
 * Higher-priority incidents are processed first within a scan pass, per
 * §3 ("Higher-priority incidents should be favored when candidates are
 * otherwise comparable").
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class UnassignedIncidentRetryScanner {

    private static final java.util.Map<String, Integer> PRIORITY_RANKS = java.util.Map.ofEntries(
            java.util.Map.entry("CRITICAL", 1),
            java.util.Map.entry("P1", 1),
            java.util.Map.entry("HIGH", 2),
            java.util.Map.entry("MAJOR", 2),
            java.util.Map.entry("P2", 2),
            java.util.Map.entry("MEDIUM", 3),
            java.util.Map.entry("MINOR", 3),
            java.util.Map.entry("P3", 3),
            java.util.Map.entry("LOW", 4),
            java.util.Map.entry("WARNING", 4),
            java.util.Map.entry("P4", 4)
    );

    private final IncidentRepository incidentRepository;
    private final AssignmentRepository assignmentRepository;
    private final AssignmentEngine assignmentEngine;

    @Scheduled(fixedDelayString = "${incident.unassigned-retry.fixed-delay-ms:120000}")
    @Transactional
    public void retryUnassigned() {
        List<Incident> unassigned = incidentRepository.findByStatusIn(List.of("NEW"));
        if (unassigned.isEmpty()) {
            return;
        }

        List<Incident> ordered = unassigned.stream()
                .sorted(Comparator.comparingInt(i -> priorityRank(i.getPriority())))
                .toList();

        int reassigned = 0;
        for (Incident incident : ordered) {
            Optional<String> engineerId = assignmentEngine.findEngineerForIncident(incident);
            if (engineerId.isEmpty()) {
                continue; // still nobody available — AssignmentEngine already re-notified Admin
            }

            Assignment assignment = Assignment.builder()
                    .incidentId(incident.getId())
                    .assigneeId(engineerId.get())
                    .assigneeType("individual")
                    .isCurrent(true)
                    .build();
            assignmentRepository.save(assignment);

            incident.setStatus("ASSIGNED");
            incidentRepository.save(incident);
            reassigned++;
        }

        if (reassigned > 0) {
            log.info("Unassigned-incident retry scan: {} incident(s) newly assigned", reassigned);
        }
    }

    private int priorityRank(String priority) {
        if (priority == null || priority.isBlank()) {
            return Integer.MAX_VALUE;
        }
        return PRIORITY_RANKS.getOrDefault(priority.trim().toUpperCase(), Integer.MAX_VALUE);
    }
}