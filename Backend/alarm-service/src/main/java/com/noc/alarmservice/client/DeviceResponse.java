package com.noc.alarmservice.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Deserialization target for device-service's /api/devices/by-device-id/{id}
 * response. Only the fields alarm-service actually needs are declared;
 * unknown fields (region, health, timestamps, etc.) are ignored so this
 * stays decoupled from device-service's full DTO.
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
}
