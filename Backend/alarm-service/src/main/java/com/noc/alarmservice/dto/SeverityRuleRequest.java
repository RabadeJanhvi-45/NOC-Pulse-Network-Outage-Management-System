package com.noc.alarmservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Request body for POST /api/alarms/rules — creates a new rule, or updates
 * the existing one if alarmType already has a rule (upsert by alarmType).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SeverityRuleRequest {

    @NotBlank(message = "alarmType is required")
    private String alarmType;

    @NotBlank(message = "severity is required")
    private String severity;

    private String conditionLogic;
}
