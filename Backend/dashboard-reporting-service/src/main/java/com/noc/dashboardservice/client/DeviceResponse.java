package com.noc.dashboardservice.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Deserialization target for device-service's device responses. Only the
 * fields dashboard-reporting-service actually needs are declared; unknown
 * fields are ignored so this stays decoupled from device-service's full DTO.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class DeviceResponse {
    private Long id;
    private String deviceId;
    private String name;
    private String deviceType;
    private String location;
    private String region;
    private String status;
    private String health;
}
