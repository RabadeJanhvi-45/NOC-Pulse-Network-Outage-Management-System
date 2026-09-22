package com.noc.dashboardservice.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Deserialization target for alarm-service's alarm responses. Only the
 * fields dashboard-reporting-service actually needs are declared; unknown
 * fields are ignored so this stays decoupled from alarm-service's full DTO.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AlarmResponse {
    private Long id;
    private String deviceId;
    private String alarmType;
    private String severity;
    private String status;
    private LocalDateTime raisedAt;
}
