package com.noc.dashboardservice.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Deserialization target for incident-service's incident responses. Only
 * the fields dashboard-reporting-service actually needs are declared;
 * unknown fields are ignored so this stays decoupled from incident-service's
 * full DTO.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class IncidentResponse {
    private Long id;
    private String deviceId;
    private Long alarmId;
    private String description;
    private String priority;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime closedAt;
}
