package com.noc.incidentservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Body for POST /api/incidents/{id}/decide-rejection.
 * decision must be "APPROVE" (re-run assignment excluding that Engineer)
 * or "REJECT" (same Engineer stays, still ASSIGNED).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RejectionDecisionRequest {

    @NotBlank(message = "decision is required")
    private String decision;
}