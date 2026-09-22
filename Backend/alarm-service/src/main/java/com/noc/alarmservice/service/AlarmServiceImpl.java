package com.noc.alarmservice.service;

import com.noc.alarmservice.client.CreateIncidentFromAlarmRequest;
import com.noc.alarmservice.client.DeviceClient;
import com.noc.alarmservice.client.IncidentClient;
import com.noc.alarmservice.dto.AlarmRequest;
import com.noc.alarmservice.dto.AlarmResponse;
import com.noc.alarmservice.entity.Alarm;
import com.noc.alarmservice.entity.AlarmAcknowledgement;
import com.noc.alarmservice.entity.SeverityRule;
import com.noc.alarmservice.exception.DeviceNotActiveException;
import com.noc.alarmservice.exception.DeviceUnavailableException;
import com.noc.alarmservice.exception.ResourceNotFoundException;
import com.noc.alarmservice.repository.AlarmAcknowledgementRepository;
import com.noc.alarmservice.repository.AlarmRepository;
import com.noc.alarmservice.repository.SeverityRuleRepository;
import feign.FeignException;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class AlarmServiceImpl implements AlarmService {

    private static final Set<String> VALID_SEVERITIES = Set.of("Critical", "Major", "Minor", "Warning");
    private static final String DEFAULT_SEVERITY = "Warning";

    private static final Map<String, String> SEVERITY_TO_PRIORITY = Map.of(
            "Critical", "P1",
            "Major", "P2",
            "Minor", "P3",
            "Warning", "P4"
    );

    private final AlarmRepository alarmRepository;
    private final AlarmAcknowledgementRepository acknowledgementRepository;
    private final SeverityRuleRepository severityRuleRepository;
    private final DeviceClient deviceClient;
    private final IncidentClient incidentClient;

    @Value("${alarm.device-validation.enabled:true}")
    private boolean deviceValidationEnabled;

    @Override
    @Transactional
    public AlarmResponse raiseAlarm(AlarmRequest request, String callerUsername) {
        if (deviceValidationEnabled) {
            validateDeviceExists(request.getDeviceId());
            validateDeviceActive(request.getDeviceId());
        }

        String severity = resolveSeverity(request.getAlarmType(), request.getSeverity());
        String groupKey = buildGroupKey(request.getDeviceId(), request.getAlarmType());
        String raisedBy = StringUtils.hasText(request.getRaisedBy())
                ? request.getRaisedBy()
                : (StringUtils.hasText(callerUsername) ? callerUsername : "system");

        // Dedup: an existing Active alarm for the same device+alarmType is the same fault —
        // don't flood the dashboard with duplicates, return the existing one instead.
        var existing = alarmRepository.findFirstByGroupKeyAndStatus(groupKey, "Active");
        if (existing.isPresent()) {
            AlarmResponse response = toResponse(existing.get());
            response.setDeduplicated(true);
            return response;
        }

        Alarm alarm = Alarm.builder()
                .deviceId(request.getDeviceId())
                .alarmType(request.getAlarmType())
                .severity(severity)
                .status("Active")
                .groupKey(groupKey)
                .raisedBy(raisedBy)
                .build();

        Alarm saved = alarmRepository.save(alarm);
        triggerIncidentCreation(saved);

        AlarmResponse response = toResponse(saved);
        response.setDeduplicated(false);
        return response;
    }

    @Override
    public List<AlarmResponse> getAlarms(String severity, String status, String deviceId, String callerRole, String callerUsername) {
        Specification<Alarm> spec = buildFilterSpec(severity, status, deviceId);
        return alarmRepository.findAll(spec).stream()
                .filter(a -> canView(a, callerRole, callerUsername))
                .map(this::toResponse)
                .toList();
    }

    private boolean canView(Alarm a, String role, String username) {
        if ("ADMIN".equalsIgnoreCase(role)) {
            return true;
        }
        if (!StringUtils.hasText(a.getRaisedBy()) || "system".equalsIgnoreCase(a.getRaisedBy())) {
            return true;
        }
        return StringUtils.hasText(username) && username.equalsIgnoreCase(a.getRaisedBy());
    }

    @Override
    public AlarmResponse getAlarmById(Long id) {
        return toResponse(findAlarmOrThrow(id));
    }

    @Override
    @Transactional
    public AlarmResponse acknowledge(Long id, String acknowledgedBy) {
        Alarm alarm = findAlarmOrThrow(id);

        AlarmAcknowledgement ack = AlarmAcknowledgement.builder()
                .alarmId(alarm.getId())
                .acknowledgedBy(StringUtils.hasText(acknowledgedBy) ? acknowledgedBy : "system")
                .build();
        acknowledgementRepository.save(ack);

        alarm.setStatus("Acknowledged");
        Alarm saved = alarmRepository.save(alarm);
        return toResponse(saved);
    }

    @Override
    @Transactional
    public AlarmResponse clearAlarm(Long id, String clearedBy) {
        Alarm alarm = findAlarmOrThrow(id);
        alarm.setStatus("Cleared");
        Alarm saved = alarmRepository.save(alarm);
        return toResponse(saved);
    }

    // ---- helpers ----

    private void validateDeviceExists(String deviceId) {
        try {
            deviceClient.getByDeviceId(deviceId);
        } catch (FeignException.NotFound ex) {
            throw new ResourceNotFoundException("Device not found with deviceId: " + deviceId);
        } catch (FeignException ex) {
            throw new DeviceUnavailableException(
                    "Could not reach device-service to validate deviceId '" + deviceId + "': " + ex.getMessage());
        }
    }

    private void validateDeviceActive(String deviceId) {
        try {
            var activeCheck = deviceClient.checkActive(deviceId);
            if (activeCheck == null || !activeCheck.isActive()) {
                throw new DeviceNotActiveException(
                        "Cannot raise an alarm: device '" + deviceId + "' is not Active");
            }
        } catch (FeignException ex) {
            throw new DeviceUnavailableException(
                    "Could not reach device-service to check active status for deviceId '" + deviceId + "': " + ex.getMessage());
        }
    }

    /**
     * Synchronous call into incident-service to auto-create (and auto-assign)
     * an Incident for this alarm. Failures here are logged, not rethrown —
     * the alarm itself has already been saved successfully and shouldn't be
     * rolled back just because incident-service is unreachable; the incident
     * side of the workflow retrying/self-healing is incident-service's
     * concern, not alarm-service's.
     */
    private void triggerIncidentCreation(Alarm alarm) {
        try {
            incidentClient.createFromAlarm(CreateIncidentFromAlarmRequest.builder()
                    .deviceId(alarm.getDeviceId())
                    .alarmId(alarm.getId())
                    .priority(SEVERITY_TO_PRIORITY.getOrDefault(alarm.getSeverity(), "P4"))
                    .description(alarm.getAlarmType() + " alarm on device " + alarm.getDeviceId())
                    .raisedBy(alarm.getRaisedBy())
                    .build());
        } catch (Exception ex) {
            log.error("Failed to trigger incident creation for alarm {}: {}", alarm.getId(), ex.getMessage());
        }
    }

    private String resolveSeverity(String alarmType, String requestedSeverity) {
        if (StringUtils.hasText(requestedSeverity)) {
            boolean valid = VALID_SEVERITIES.stream().anyMatch(s -> s.equalsIgnoreCase(requestedSeverity));
            if (!valid) {
                throw new IllegalArgumentException(
                        "severity must be one of " + VALID_SEVERITIES + " but was '" + requestedSeverity + "'");
            }
            return requestedSeverity;
        }

        return severityRuleRepository.findByAlarmTypeIgnoreCase(alarmType)
                .map(SeverityRule::getSeverity)
                .orElse(DEFAULT_SEVERITY);
    }

    private String buildGroupKey(String deviceId, String alarmType) {
        return deviceId + "::" + alarmType;
    }

    private Alarm findAlarmOrThrow(Long id) {
        return alarmRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Alarm not found with id: " + id));
    }

    private Specification<Alarm> buildFilterSpec(String severity, String status, String deviceId) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (StringUtils.hasText(severity)) {
                predicates.add(cb.equal(cb.lower(root.get("severity")), severity.toLowerCase()));
            }
            if (StringUtils.hasText(status)) {
                if (!"all".equalsIgnoreCase(status)) {
                    predicates.add(cb.equal(cb.lower(root.get("status")), status.toLowerCase()));
                }
            } else {
                predicates.add(cb.not(cb.lower(root.get("status")).in("cleared", "closed")));
            }
            if (StringUtils.hasText(deviceId)) {
                predicates.add(cb.equal(cb.lower(root.get("deviceId")), deviceId.toLowerCase()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private AlarmResponse toResponse(Alarm a) {
        return AlarmResponse.builder()
                .id(a.getId())
                .deviceId(a.getDeviceId())
                .alarmType(a.getAlarmType())
                .severity(a.getSeverity())
                .status(a.getStatus())
                .groupKey(a.getGroupKey())
                .raisedBy(a.getRaisedBy())
                .raisedAt(a.getRaisedAt())
                .build();
    }
}