package com.noc.incidentservice.service;

import com.noc.incidentservice.client.AlarmServiceClient;
import com.noc.incidentservice.client.AuthServiceClient;
import com.noc.incidentservice.client.DeviceServiceClient;
import com.noc.incidentservice.client.NotificationServiceClient;
import com.noc.incidentservice.dto.*;
import com.noc.incidentservice.entity.*;
import com.noc.incidentservice.exception.IncidentClosedException;
import com.noc.incidentservice.exception.InvalidDeviceException;
import com.noc.incidentservice.exception.ResourceNotFoundException;
import com.noc.incidentservice.exception.UpstreamServiceUnavailableException;
import com.noc.incidentservice.repository.*;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class IncidentServiceImpl implements IncidentService {

    private static final Set<String> VALID_PRIORITIES = Set.of("P1", "P2", "P3", "P4");
    private static final Set<String> CLOSED_STATUSES = Set.of("CLOSED");

    private final IncidentRepository incidentRepository;
    private final IncidentHistoryRepository incidentHistoryRepository;
    private final AssignmentRepository assignmentRepository;
    private final AssignmentHistoryRepository assignmentHistoryRepository;
    private final ProgressNoteRepository progressNoteRepository;
    private final SlaTrackingRepository slaTrackingRepository;
    private final EngineerRejectionRepository engineerRejectionRepository;
    private final DeviceServiceClient deviceServiceClient;
    private final AssignmentEngine assignmentEngine;
    private final NotificationServiceClient notificationServiceClient;
    private final AuthServiceClient authServiceClient;
    private final AlarmServiceClient alarmServiceClient;

    @Value("${incident.sla.p1-minutes:60}")
    private int p1SlaMinutes;

    @Value("${incident.sla.p2-minutes:240}")
    private int p2SlaMinutes;

    @Value("${incident.sla.p3-minutes:1440}")
    private int p3SlaMinutes;

    @Value("${incident.sla.p4-minutes:2880}")
    private int p4SlaMinutes;

    // ---- create / read / update ----

    @Override
    @Transactional
    public IncidentResponse createFromAlarm(CreateIncidentFromAlarmRequest request) {
        assertDeviceExists(request.getDeviceId());
        String priority = validatePriority(request.getPriority());

        Incident incident = Incident.builder()
                .deviceId(request.getDeviceId())
                .alarmId(request.getAlarmId())
                .description(request.getDescription())
                .priority(priority)
                .status("NEW")
                .createdBy(StringUtils.hasText(request.getRaisedBy()) ? request.getRaisedBy() : "system")
                .build();

        Incident saved = incidentRepository.save(incident);

        SlaTracking sla = SlaTracking.builder()
                .incidentId(saved.getId())
                .slaThreshold(resolveSlaThreshold(priority))
                .resolutionTime(0)
                .isBreached(false)
                .build();
        slaTrackingRepository.save(sla);

        assignmentEngine.findEngineerForIncident(saved).ifPresent(engineerId -> {
            createAssignment(saved, engineerId, "system");
            changeStatus(saved, "ASSIGNED", "system");
            incidentRepository.save(saved);
            notifyUser(engineerId, "INCIDENT_ASSIGNED", "Incident #" + saved.getId() + " was assigned to you.");
        });
        return toResponse(saved);
    }

    @Override
    public List<IncidentResponse> getIncidents(String status, String priority, String callerRole, Long callerUserId, String callerUsername) {
        Specification<Incident> spec = buildFilterSpec(status, priority);
        return incidentRepository.findAll(spec).stream()
                .filter(i -> canView(i, callerRole, callerUserId, callerUsername))
                .map(this::toResponse)
                .toList();
    }

    @Override
    public IncidentResponse getIncidentById(Long id, String callerRole, Long callerUserId, String callerUsername) {
        Incident incident = findIncidentOrThrow(id);
        if (!canView(incident, callerRole, callerUserId, callerUsername)) {
            throw new ResourceNotFoundException("Incident not found with id: " + id);
        }
        return toResponse(incident);
    }

    @Override
    public List<IncidentHistoryResponse> getHistory(Long incidentId) {
        findIncidentOrThrow(incidentId); // 404 if the incident itself doesn't exist
        return incidentHistoryRepository.findByIncidentIdOrderByChangedAtDesc(incidentId).stream()
                .map(this::toHistoryResponse)
                .toList();
    }

    @Override
    public List<AssignmentResponse> getAssignments(Long incidentId) {
        findIncidentOrThrow(incidentId);
        return assignmentRepository.findByIncidentIdOrderByAssignedAtDesc(incidentId).stream()
                .map(this::toAssignmentResponse)
                .toList();
    }

    @Override
    public List<ProgressNoteResponse> getProgressNotes(Long incidentId) {
        findIncidentOrThrow(incidentId);
        return progressNoteRepository.findByIncidentIdOrderByCreatedAtDesc(incidentId).stream()
                .map(this::toProgressNoteResponse)
                .toList();
    }

    @Override
    @Transactional
    public IncidentResponse startWork(Long incidentId, Long engineerId) {
        Incident incident = findIncidentOrThrow(incidentId);
        assertEngineerOwns(incidentId, engineerId);
        assertNotClosed(incident);
        requireStatus(incident, "ASSIGNED");
        changeStatus(incident, "IN_PROGRESS", String.valueOf(engineerId));
        return toResponse(incidentRepository.save(incident));
    }

    @Override
    @Transactional
    public IncidentResponse resolveIncident(Long incidentId, Long engineerId, ResolveRequest request) {
        Incident incident = findIncidentOrThrow(incidentId);
        assertEngineerOwns(incidentId, engineerId);
        assertNotClosed(incident);
        requireStatus(incident, "IN_PROGRESS");
        if (request != null) {
            incident.setResolutionNotes(request.getResolutionNotes());
        }
        changeStatus(incident, "RESOLVED", String.valueOf(engineerId));
        return toResponse(incidentRepository.save(incident));
    }

    @Override
    @Transactional
    public EngineerRejectionResponse rejectAssignment(Long incidentId, Long engineerId, ReasonRequest request) {
        Incident incident = findIncidentOrThrow(incidentId);
        assertEngineerOwns(incidentId, engineerId);
        assertNotClosed(incident);
        requireStatus(incident, "ASSIGNED");
        requireReason(request);
        EngineerRejection rejection = engineerRejectionRepository.save(EngineerRejection.builder()
                .incidentId(incidentId).engineerId(String.valueOf(engineerId)).reason(request.getReason()).build());
        notifyRole("ADMIN", "ENGINEER_REJECTION", "Engineer " + engineerId + " rejected incident #" + incidentId);
        return toRejectionResponse(rejection);
    }

    @Override
    @Transactional
    public ProgressNoteResponse addProgressNote(Long incidentId, ProgressNoteRequest request) {
        Incident incident = findIncidentOrThrow(incidentId);
        assertNotClosed(incident);
        assertEngineerOwns(incidentId, request.getAuthorId());
        return toProgressNoteResponse(progressNoteRepository.save(ProgressNote.builder()
                .incidentId(incidentId).noteText(request.getNoteText()).authorId(request.getAuthorId()).build()));
    }

    @Override
    @Transactional
    public IncidentResponse verifyIncident(Long incidentId, String closedBy) {
        Incident incident = findIncidentOrThrow(incidentId);
        assertNotClosed(incident);
        requireStatus(incident, "RESOLVED");
        incident.setClosedBy(closedBy);
        incident.setClosedAt(LocalDateTime.now());
        changeStatus(incident, "CLOSED", closedBy);
        updateSlaOnClose(incident);

        if (incident.getAlarmId() != null) {
            try {
                alarmServiceClient.clearAlarm(incident.getAlarmId());
            } catch (Exception ex) {
                // Log and proceed if alarm-service is temporarily unavailable
            }
        }

        return toResponse(incidentRepository.save(incident));
    }

    @Override
    @Transactional
    public IncidentResponse rejectResolution(Long incidentId, ReasonRequest request) {
        Incident incident = findIncidentOrThrow(incidentId);
        assertNotClosed(incident);
        requireStatus(incident, "RESOLVED");
        requireReason(request);
        incident.setResolutionNotes("NOC rejection: " + request.getReason());
        changeStatus(incident, "IN_PROGRESS", "NOC_OPERATOR");
        return toResponse(incidentRepository.save(incident));
    }

    @Override
    @Transactional
    public IncidentResponse decideRejection(Long incidentId, Long adminUserId, RejectionDecisionRequest request) {
        Incident incident = findIncidentOrThrow(incidentId);
        assertNotClosed(incident);
        EngineerRejection rejection = engineerRejectionRepository
                .findFirstByIncidentIdAndAdminDecisionIsNullOrderByRejectedAtDesc(incidentId)
                .orElseThrow(() -> new ResourceNotFoundException("No pending engineer rejection for incident: " + incidentId));
        String decision = request.getDecision().toUpperCase();
        if (!Set.of("APPROVE", "REJECT").contains(decision)) {
            throw new IllegalArgumentException("decision must be APPROVE or REJECT");
        }
        rejection.setAdminDecision(decision);
        rejection.setDecidedBy(String.valueOf(adminUserId));
        rejection.setDecidedAt(LocalDateTime.now());
        engineerRejectionRepository.save(rejection);
        if ("APPROVE".equals(decision)) {
            assignmentRepository.findFirstByIncidentIdAndIsCurrentTrue(incidentId).ifPresent(current -> {
                current.setIsCurrent(false);
                assignmentRepository.save(current);
            });
            changeStatus(incident, "NEW", String.valueOf(adminUserId));
            Set<String> previouslyRejectedBy = engineerRejectionRepository.findByIncidentId(incidentId).stream()
                    .map(EngineerRejection::getEngineerId)
                    .collect(java.util.stream.Collectors.toSet());
            assignmentEngine.findEngineerForIncident(incident, previouslyRejectedBy).ifPresent(engineerId -> {
                createAssignment(incident, engineerId, String.valueOf(adminUserId));
                changeStatus(incident, "ASSIGNED", String.valueOf(adminUserId));
                notifyUser(engineerId, "INCIDENT_ASSIGNED", "Incident #" + incidentId + " was assigned to you.");
            });
        }
        return toResponse(incidentRepository.save(incident));
    }

    // ---- SLA ----

    @Override
    public SlaResponse getSlaStatus(Long incidentId) {
        findIncidentOrThrow(incidentId);
        SlaTracking sla = slaTrackingRepository.findByIncidentId(incidentId)
                .orElseThrow(() -> new ResourceNotFoundException("No SLA record found for incident: " + incidentId));
        return toSlaResponse(sla);
    }

    // ---- helpers ----

    /** Confirms deviceId exists in device-service before an incident is saved. */
    private void assertDeviceExists(String deviceId) {
        try {
            deviceServiceClient.getDeviceByBusinessId(deviceId);
        } catch (FeignException.NotFound ex) {
            throw new InvalidDeviceException("No device found with deviceId: " + deviceId);
        } catch (FeignException ex) {
            throw new UpstreamServiceUnavailableException(
                    "Could not reach device-service to validate deviceId '" + deviceId + "': " + ex.getMessage());
        }
    }

    private String validatePriority(String priority) {
        return VALID_PRIORITIES.stream()
                .filter(p -> p.equalsIgnoreCase(priority))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "priority must be one of " + VALID_PRIORITIES + " but was '" + priority + "'"));
    }

    private int resolveSlaThreshold(String priority) {
        return switch (priority) {
            case "P1" -> p1SlaMinutes;
            case "P2" -> p2SlaMinutes;
            case "P3" -> p3SlaMinutes;
            default -> p4SlaMinutes;
        };
    }

    private void assertNotClosed(Incident incident) {
        if (CLOSED_STATUSES.contains(incident.getStatus())) {
            throw new IncidentClosedException("Incident is closed and cannot be modified");
        }
    }

    private void requireStatus(Incident incident, String expected) {
        if (!expected.equals(incident.getStatus())) {
            throw new IllegalStateException("Incident must be " + expected + " but was " + incident.getStatus());
        }
    }

    private void requireReason(ReasonRequest request) {
        if (request == null || !StringUtils.hasText(request.getReason())) {
            throw new IllegalArgumentException("reason is required");
        }
    }

    private void changeStatus(Incident incident, String status, String changedBy) {
        logChange(incident.getId(), "status", incident.getStatus(), status, changedBy);
        incident.setStatus(status);
    }

    private void createAssignment(Incident incident, String engineerId, String assignedBy) {
        String previous = assignmentRepository.findFirstByIncidentIdAndIsCurrentTrue(incident.getId())
                .map(current -> {
                    current.setIsCurrent(false);
                    assignmentRepository.save(current);
                    return current.getAssigneeId();
                }).orElse(null);
        assignmentRepository.save(Assignment.builder().incidentId(incident.getId()).assigneeId(engineerId)
                .assigneeType("individual").isCurrent(true).build());
        assignmentHistoryRepository.save(AssignmentHistory.builder().incidentId(incident.getId())
                .previousAssignee(previous).newAssignee(engineerId).reassignedBy(assignedBy).build());
    }

    private boolean canView(Incident incident, String callerRole, Long callerUserId, String callerUsername) {
        if ("ADMIN".equalsIgnoreCase(callerRole)) {
            return true;
        }
        if ("ENGINEER".equalsIgnoreCase(callerRole)) {
            return callerUserId != null && assignmentRepository.findFirstByIncidentIdAndIsCurrentTrue(incident.getId())
                    .map(assignment -> String.valueOf(callerUserId).equals(assignment.getAssigneeId())).orElse(false);
        }
        if ("NOC_OPERATOR".equalsIgnoreCase(callerRole)) {
            if (!StringUtils.hasText(incident.getCreatedBy()) || "system".equalsIgnoreCase(incident.getCreatedBy())) {
                return true;
            }
            return StringUtils.hasText(callerUsername) && callerUsername.equalsIgnoreCase(incident.getCreatedBy());
        }
        return true;
    }

    private void assertEngineerOwns(Long incidentId, Long engineerId) {
        assertEngineerOwns(incidentId, engineerId != null ? String.valueOf(engineerId) : null);
    }

    private void assertEngineerOwns(Long incidentId, String engineerId) {
        if (!StringUtils.hasText(engineerId) || assignmentRepository.findFirstByIncidentIdAndIsCurrentTrue(incidentId)
                .map(assignment -> !engineerId.equals(assignment.getAssigneeId())).orElse(true)) {
            throw new ResourceNotFoundException("Incident is not assigned to this Engineer");
        }
    }

    private void notifyUser(String engineerId, String type, String message) {
        notificationServiceClient.create(CreateNotificationRequest.builder().userId(Long.valueOf(engineerId))
                .type(type).message(message).build());
    }

    private void notifyRole(String role, String type, String message) {
        notificationServiceClient.create(CreateNotificationRequest.builder().role(role).type(type).message(message).build());
    }

    /** Recomputes resolutionTime/isBreached on the SlaTracking row once an incident is closed. */
    private void updateSlaOnClose(Incident incident) {
        slaTrackingRepository.findByIncidentId(incident.getId()).ifPresent(sla -> {
            LocalDateTime start = incident.getCreatedAt() != null ? incident.getCreatedAt() : LocalDateTime.now();
            LocalDateTime end = incident.getClosedAt() != null ? incident.getClosedAt() : LocalDateTime.now();
            long minutesTaken = Duration.between(start, end).toMinutes();
            sla.setResolutionTime((int) minutesTaken);
            sla.setIsBreached(minutesTaken > sla.getSlaThreshold());
            slaTrackingRepository.save(sla);
        });
    }

    private void logChange(Long incidentId, String field, String oldValue, String newValue, String changedBy) {
        IncidentHistory history = IncidentHistory.builder()
                .incidentId(incidentId)
                .fieldChanged(field)
                .oldValue(oldValue)
                .newValue(newValue)
                .changedBy(changedBy)
                .build();
        incidentHistoryRepository.save(history);
    }

    private Incident findIncidentOrThrow(Long id) {
        return incidentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Incident not found with id: " + id));
    }

    private Specification<Incident> buildFilterSpec(String status, String priority) {
        return (root, query, cb) -> {
            List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();

            if (StringUtils.hasText(status)) {
                predicates.add(cb.equal(cb.lower(root.get("status")), status.toLowerCase()));
            }
            if (StringUtils.hasText(priority)) {
                predicates.add(cb.equal(cb.lower(root.get("priority")), priority.toLowerCase()));
            }

            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };
    }

    private IncidentResponse toResponse(Incident i) {
        String assigneeId = null;
        String assignedEngineer = "Unassigned";
        String assignedEngineerUsername = null;
        String assignedEngineerSpecialization = null;

        var assignmentOpt = assignmentRepository.findFirstByIncidentIdAndIsCurrentTrue(i.getId());
        if (assignmentOpt.isPresent()) {
            assigneeId = assignmentOpt.get().getAssigneeId();
            if (StringUtils.hasText(assigneeId)) {
                try {
                    List<EngineerResponse> engineers = authServiceClient.getEngineers();
                    if (engineers != null) {
                        for (EngineerResponse eng : engineers) {
                            if (eng.getUserId() != null && String.valueOf(eng.getUserId()).equals(assigneeId)) {
                                assignedEngineer = StringUtils.hasText(eng.getFullName()) ? eng.getFullName() : eng.getUsername();
                                assignedEngineerUsername = eng.getUsername();
                                assignedEngineerSpecialization = eng.getPrimarySpecialization();
                                break;
                            }
                        }
                    }
                } catch (Exception ex) {
                    assignedEngineer = "Engineer #" + assigneeId;
                }
                if ("Unassigned".equals(assignedEngineer)) {
                    assignedEngineer = "Engineer #" + assigneeId;
                }
            }
        }

        return IncidentResponse.builder()
                .id(i.getId())
                .deviceId(i.getDeviceId())
                .alarmId(i.getAlarmId())
                .description(i.getDescription())
                .priority(i.getPriority())
                .status(i.getStatus())
                .resolutionNotes(i.getResolutionNotes())
                .createdBy(i.getCreatedBy())
                .assigneeId(assigneeId)
                .assignedEngineer(assignedEngineer)
                .assignedEngineerUsername(assignedEngineerUsername)
                .assignedEngineerSpecialization(assignedEngineerSpecialization)
                .createdAt(i.getCreatedAt())
                .closedAt(i.getClosedAt())
                .build();
    }

    private IncidentHistoryResponse toHistoryResponse(IncidentHistory h) {
        return IncidentHistoryResponse.builder()
                .id(h.getId())
                .incidentId(h.getIncidentId())
                .fieldChanged(h.getFieldChanged())
                .oldValue(h.getOldValue())
                .newValue(h.getNewValue())
                .changedBy(h.getChangedBy())
                .changedAt(h.getChangedAt())
                .build();
    }

    private AssignmentResponse toAssignmentResponse(Assignment a) {
        return AssignmentResponse.builder()
                .id(a.getId())
                .incidentId(a.getIncidentId())
                .assigneeId(a.getAssigneeId())
                .assigneeType(a.getAssigneeType())
                .assignedAt(a.getAssignedAt())
                .isCurrent(a.getIsCurrent())
                .build();
    }

    private ProgressNoteResponse toProgressNoteResponse(ProgressNote n) {
        return ProgressNoteResponse.builder()
                .id(n.getId())
                .incidentId(n.getIncidentId())
                .noteText(n.getNoteText())
                .authorId(n.getAuthorId())
                .createdAt(n.getCreatedAt())
                .build();
    }

    private EscalationResponse toEscalationResponse(Escalation e) {
        return EscalationResponse.builder()
                .id(e.getId())
                .incidentId(e.getIncidentId())
                .escalatedBy(e.getEscalatedBy())
                .reason(e.getReason())
                .triggerType(e.getTriggerType())
                .escalatedAt(e.getEscalatedAt())
                .build();
    }

    private EngineerRejectionResponse toRejectionResponse(EngineerRejection r) {
        return EngineerRejectionResponse.builder()
                .id(r.getId())
                .incidentId(r.getIncidentId())
                .engineerId(r.getEngineerId())
                .reason(r.getReason())
                .adminDecision(r.getAdminDecision())
                .decidedBy(r.getDecidedBy())
                .rejectedAt(r.getRejectedAt())
                .decidedAt(r.getDecidedAt())
                .build();
    }

    private SlaResponse toSlaResponse(SlaTracking s) {
        return SlaResponse.builder()
                .incidentId(s.getIncidentId())
                .slaThreshold(s.getSlaThreshold())
                .resolutionTime(s.getResolutionTime())
                .isBreached(s.getIsBreached())
                .build();
    }
}