package com.noc.dashboardservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * Feign client into device-service, resolved by its Eureka-registered
 * application name ("device-service") — no hardcoded host/port, per the
 * cross-service communication rule.
 */
@FeignClient(name = "device-service")
public interface DeviceClient {

    /** Full device inventory — used for the "total devices" summary count. */
    @GetMapping("/api/devices")
    List<DeviceResponse> getDevices(
            @RequestParam(value = "region", required = false) String region,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "deviceType", required = false) String deviceType,
            @RequestParam(value = "search", required = false) String search);

    /** Lookup by business deviceId — used to enrich per-device reports with device details. */
    @GetMapping("/api/devices/by-device-id/{deviceId}")
    DeviceResponse getByDeviceId(@PathVariable("deviceId") String deviceId);
}
