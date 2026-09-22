package com.noc.deviceservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeviceTypeSpecializationRequest {

    @NotBlank(message = "deviceType is required")
    private String deviceType;

    @NotBlank(message = "requiredSpecialization is required")
    private String requiredSpecialization;
}