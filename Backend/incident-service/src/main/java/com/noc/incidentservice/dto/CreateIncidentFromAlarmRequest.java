package com.noc.incidentservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Body posted by alarm-service to POST /api/incidents/internal/from-alarm
 * (see alarm-service's IncidentClient / CreateIncidentFromAlarmRequest —
 * this is the receiving side of that exact contract). Priority arrives
 * already mapped from severity: P1 (Critical) / P2 (Major) / P3 (Minor) /
 * P4 (Warning).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateIncidentFromAlarmRequest {

    @NotBlank
    private String deviceId;

    @NotNull
    private Long alarmId;

    @NotBlank
    private String priority;

    private String description;

    private String raisedBy;
}