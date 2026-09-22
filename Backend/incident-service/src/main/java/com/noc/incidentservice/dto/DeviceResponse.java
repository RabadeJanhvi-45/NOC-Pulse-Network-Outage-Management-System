package com.noc.incidentservice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Client-side view of device-service's DeviceResponse — only the fields
 * incident-service actually needs from GET /api/devices/by-device-id/{deviceId}.
 * Extra fields in the real response are simply ignored during deserialization.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DeviceResponse {

    private Long id;
    private String deviceId;
    private String deviceType;
    private String status;
}