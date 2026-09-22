package com.noc.incidentservice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Used to update an incident's own fields (PUT /api/incidents/{id}).
 * Manual incident creation was removed — incidents are only ever created
 * via POST /api/incidents/internal/from-alarm (see CreateIncidentFromAlarmRequest).
 * Status transitions driven by the workflow (start/resolve/verify/etc.) go
 * through their own dedicated endpoints, not this general update path —
 * this stays for priority/description/resolutionNotes edits.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class IncidentRequest {

    private String priority;

    private String description;

    private String resolutionNotes;
}