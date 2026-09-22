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
public class DeviceResponse {

    private Long id;
    private String deviceId;
    private String deviceType;
    private String name;
    private String ipAddress;
    private String location;
    private String region;
    private String status;
    private String health;
    private String createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
