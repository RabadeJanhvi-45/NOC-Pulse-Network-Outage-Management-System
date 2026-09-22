package com.noc.incidentservice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Client-side view of device-service's DeviceTypeSpecializationResponse. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DeviceTypeSpecializationResponse {

    private Long id;
    private String deviceType;
    private String requiredSpecialization;
}