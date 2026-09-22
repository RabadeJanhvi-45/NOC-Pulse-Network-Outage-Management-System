package com.noc.deviceservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeviceRegistrationRequestResponse {

    private Long id;
    private String requestType;
    private Long targetDeviceId;
    private String deviceId;
    private String deviceType;
    private String name;
    private String ipAddress;
    private String location;
    private String region;
    private String requestedBy;
    private String status;
    private String rejectionReason;
    private String reviewedBy;
    private LocalDateTime requestedAt;
    private LocalDateTime reviewedAt;
}