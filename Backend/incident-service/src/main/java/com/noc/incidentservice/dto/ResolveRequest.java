package com.noc.incidentservice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Body for POST /api/incidents/{id}/resolve — resolutionNotes is optional. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ResolveRequest {

    private String resolutionNotes;
}