package com.noc.incidentservice.client;

import com.noc.incidentservice.dto.DeviceResponse;
import com.noc.incidentservice.dto.DeviceTypeSpecializationResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Calls device-service by its Eureka-registered application name
 * ("device-service") rather than a hardcoded URL. Per the cross-service
 * communication rule, incident-service never queries device-db directly.
 */
@FeignClient(name = "device-service")
public interface DeviceServiceClient {

    @GetMapping("/api/devices/by-device-id/{deviceId}")
    DeviceResponse getDeviceByBusinessId(@PathVariable("deviceId") String deviceId);

    /** Used by the AssignmentEngine to find which specialization a device's type requires. */
    @GetMapping("/api/device-type-specializations/by-type/{deviceType}")
    DeviceTypeSpecializationResponse getRequiredSpecialization(@PathVariable("deviceType") String deviceType);
}