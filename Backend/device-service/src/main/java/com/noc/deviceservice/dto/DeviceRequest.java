package com.noc.deviceservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Request body for POST /api/devices (register) and PUT /api/devices/{id} (edit).
 * deviceId/deviceType are required at registration; PUT typically only touches
 * name/ipAddress/location/region/status per US-05, but all fields are accepted
 * so the same DTO can serve both operations.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeviceRequest {

    @NotBlank(message = "deviceId is required")
    private String deviceId;

    @NotBlank(message = "deviceType is required")
    private String deviceType;

    @NotBlank(message = "name is required")
    private String name;

    private String ipAddress;

    private String location;

    private String region;

    /** Active / Inactive / Faulty — defaults to Active if omitted on registration. */
    private String status;

    /** Healthy / Degraded / Critical — defaults to Healthy if omitted on registration. */
    private String health;

    private String createdBy;
}
