package com.noc.incidentservice.service;

import com.noc.incidentservice.dto.*;

import java.util.List;

public interface IncidentService {

    // ---------------------------------------------------------
    // Incident creation
    // ---------------------------------------------------------

    /**
     * Internal endpoint called by alarm-service.
     *
     * Alarm -> Incident -> automatic Engineer assignment.
     */
    IncidentResponse createFromAlarm(CreateIncidentFromAlarmRequest request);


    // ---------------------------------------------------------
    // Incident visibility
    // ---------------------------------------------------------

    List<IncidentResponse> getIncidents(
            String status,
            String priority,
            String callerRole,
            Long callerUserId,
            String callerUsername
    );

    IncidentResponse getIncidentById(
            Long id,
            String callerRole,
            Long callerUserId,
            String callerUsername
    );


    // ---------------------------------------------------------
    // Incident read-only supporting data
    // ---------------------------------------------------------

    List<IncidentHistoryResponse> getHistory(Long incidentId);

    List<AssignmentResponse> getAssignments(Long incidentId);

    List<ProgressNoteResponse> getProgressNotes(Long incidentId);

    SlaResponse getSlaStatus(Long incidentId);


    // ---------------------------------------------------------
    // Engineer workflow
    // ---------------------------------------------------------

    /**
     * ASSIGNED -> IN_PROGRESS
     */
    IncidentResponse startWork(
            Long incidentId,
            Long engineerId
    );

    /**
     * IN_PROGRESS -> RESOLVED
     */
    IncidentResponse resolveIncident(
            Long incidentId,
            Long engineerId,
            ResolveRequest request
    );

    /**
     * Engineer rejects assignment.
     *
     * Incident remains ASSIGNED.
     * Admin decision becomes pending.
     */
    EngineerRejectionResponse rejectAssignment(
            Long incidentId,
            Long engineerId,
            ReasonRequest request
    );

    /**
     * Engineer adds a progress note.
     */
    ProgressNoteResponse addProgressNote(
            Long incidentId,
            ProgressNoteRequest request
    );


    // ---------------------------------------------------------
    // NOC workflow
    // ---------------------------------------------------------

    /**
     * RESOLVED -> CLOSED
     */
    IncidentResponse verifyIncident(
            Long incidentId,
            String closedBy
    );

    /**
     * RESOLVED -> IN_PROGRESS
     *
     * Same Engineer remains assigned.
     */
    IncidentResponse rejectResolution(
            Long incidentId,
            ReasonRequest request
    );


    // ---------------------------------------------------------
    // ADMIN workflow
    // ---------------------------------------------------------

    /**
     * Handles an Engineer assignment rejection.
     *
     * APPROVE -> reassign to another Engineer.
     * REJECT  -> same Engineer continues.
     */
    IncidentResponse decideRejection(
            Long incidentId,
            Long adminUserId,
            RejectionDecisionRequest request
    );
}