package com.noc.incidentservice.controller;

import com.noc.incidentservice.dto.*;
import com.noc.incidentservice.service.IncidentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST endpoints for incident-service (Backend Guide §5.2).
 * Reachable through api-gateway at /api/incidents/**.
 */
@RestController
@RequestMapping("/api/incidents")
@RequiredArgsConstructor
public class IncidentController {

    private final IncidentService incidentService;

        /** Internal-only: alarm-service calls this after saving a new Alarm. Creates the Incident and runs the assignment engine. */
    @PostMapping("/internal/from-alarm")
    public ResponseEntity<IncidentResponse> createFromAlarm(@Valid @RequestBody CreateIncidentFromAlarmRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(incidentService.createFromAlarm(request));
    }

    /** List incidents, optionally filtered by status/priority. */
    @GetMapping
    public ResponseEntity<List<IncidentResponse>> getIncidents(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String priority,
            Authentication authentication) {
        return ResponseEntity.ok(incidentService.getIncidents(
                status, priority, currentRole(authentication), currentUserId(authentication), currentUsername(authentication)));
    }

    /** Incident detail. */
    @GetMapping("/{id}")
    public ResponseEntity<IncidentResponse> getIncidentById(
            @PathVariable Long id,
            Authentication authentication) {
        return ResponseEntity.ok(incidentService.getIncidentById(
                id, currentRole(authentication), currentUserId(authentication), currentUsername(authentication)));
    }

    @PostMapping("/{id}/start")
    public ResponseEntity<IncidentResponse> startWork(@PathVariable Long id, Authentication authentication) {
        return ResponseEntity.ok(incidentService.startWork(id, currentUserId(authentication)));
    }

    @PostMapping("/{id}/resolve")
    public ResponseEntity<IncidentResponse> resolve(@PathVariable Long id, Authentication authentication,
                                                     @RequestBody(required = false) ResolveRequest request) {
        return ResponseEntity.ok(incidentService.resolveIncident(id, currentUserId(authentication), request));
    }

    @PostMapping("/{id}/reject-assignment")
    public ResponseEntity<EngineerRejectionResponse> rejectAssignment(@PathVariable Long id,
                                                                        Authentication authentication,
                                                                        @Valid @RequestBody ReasonRequest request) {
        return ResponseEntity.ok(incidentService.rejectAssignment(id, currentUserId(authentication), request));
    }

    @PostMapping("/{id}/notes")
    public ResponseEntity<ProgressNoteResponse> addProgressNote(@PathVariable Long id,
                                                                 @Valid @RequestBody ProgressNoteRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(incidentService.addProgressNote(id, request));
    }

    @PostMapping("/{id}/verify")
    public ResponseEntity<IncidentResponse> verify(@PathVariable Long id, Authentication authentication) {
        return ResponseEntity.ok(incidentService.verifyIncident(id, String.valueOf(currentUserId(authentication))));
    }

    @PostMapping("/{id}/reject-resolution")
    public ResponseEntity<IncidentResponse> rejectResolution(@PathVariable Long id, @Valid @RequestBody ReasonRequest request) {
        return ResponseEntity.ok(incidentService.rejectResolution(id, request));
    }

    @PostMapping("/{id}/decide-rejection")
    public ResponseEntity<IncidentResponse> decideRejection(@PathVariable Long id,
                                                              Authentication authentication,
                                                              @Valid @RequestBody RejectionDecisionRequest request) {
        return ResponseEntity.ok(incidentService.decideRejection(id, currentUserId(authentication), request));
    }

    /** Field-level change history for an incident. */
    @GetMapping("/{id}/history")
    public ResponseEntity<List<IncidentHistoryResponse>> getHistory(@PathVariable Long id) {
        return ResponseEntity.ok(incidentService.getHistory(id));
    }

   
    /** Full assignment history for an incident, most recent first. */
    @GetMapping("/{id}/assignments")
    public ResponseEntity<List<AssignmentResponse>> getAssignments(@PathVariable Long id) {
        return ResponseEntity.ok(incidentService.getAssignments(id));
    }

    /** List progress notes, most recent first. */
    @GetMapping("/{id}/notes")
    public ResponseEntity<List<ProgressNoteResponse>> getProgressNotes(@PathVariable Long id) {
        return ResponseEntity.ok(incidentService.getProgressNotes(id));
    }

    /** Current SLA status for an incident. */
    @GetMapping("/{id}/sla")
    public ResponseEntity<SlaResponse> getSlaStatus(@PathVariable Long id) {
        return ResponseEntity.ok(incidentService.getSlaStatus(id));
    }

    // ---- helpers: identity comes from the validated JWT (SecurityContext), never from client headers ----

    private String currentRole(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .findFirst()
                .map(a -> a.getAuthority().replace("ROLE_", ""))
                .orElse(null);
    }

    private Long currentUserId(Authentication authentication) {
        return (Long) authentication.getDetails();
    }

    private String currentUsername(Authentication authentication) {
        return authentication != null ? authentication.getName() : null;
    }
}