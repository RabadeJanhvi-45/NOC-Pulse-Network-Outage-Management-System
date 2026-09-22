package com.noc.alarmservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Request body for POST /api/alarms. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlarmRequest {

    @NotBlank(message = "deviceId is required")
    private String deviceId;

    @NotBlank(message = "alarmType is required")
    private String alarmType;

    /**
     * Optional — Critical / Major / Minor / Warning. If omitted, resolved
     * from a matching SeverityRule, falling back to "Warning" if none matches.
     */
    private String severity;

    private String raisedBy;
}
