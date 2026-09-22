package com.noc.alarmservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Feign client into device-service, looked up by its Eureka-registered
 * application name (see device-service application.yml spring.application.name)
 * — no hardcoded host/port.
 */
@FeignClient(name = "device-service")
public interface DeviceClient {

    @GetMapping("/api/devices/by-device-id/{deviceId}")
    DeviceResponse getByDeviceId(@PathVariable("deviceId") String deviceId);

    @GetMapping("/api/devices/by-device-id/{deviceId}/active-check")
    DeviceActiveResponse checkActive(@PathVariable("deviceId") String deviceId);
}
