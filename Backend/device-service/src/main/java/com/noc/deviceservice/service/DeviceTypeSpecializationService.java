package com.noc.deviceservice.service;

import com.noc.deviceservice.dto.DeviceTypeSpecializationRequest;
import com.noc.deviceservice.dto.DeviceTypeSpecializationResponse;

import java.util.List;
import java.util.Optional;

public interface DeviceTypeSpecializationService {

    DeviceTypeSpecializationResponse create(DeviceTypeSpecializationRequest request);

    List<DeviceTypeSpecializationResponse> getAll();

    DeviceTypeSpecializationResponse update(Long id, DeviceTypeSpecializationRequest request);

    void delete(Long id);

    /** Used by the internal lookup endpoint — incident-service's assignment engine calls this via Feign. */
    Optional<DeviceTypeSpecializationResponse> findByDeviceType(String deviceType);
}