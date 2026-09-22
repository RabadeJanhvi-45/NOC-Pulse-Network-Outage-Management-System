package com.noc.deviceservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Body for POST /api/device-requests — NOC_OPERATOR submitting a new-device request for Admin approval. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeviceRequestSubmission {

    @NotBlank(message = "deviceId is required")
    private String deviceId;

    @NotBlank(message = "deviceType is required")
    private String deviceType;

    @NotBlank(message = "name is required")
    private String name;

    private String ipAddress;

    private String location;

    private String region;
}