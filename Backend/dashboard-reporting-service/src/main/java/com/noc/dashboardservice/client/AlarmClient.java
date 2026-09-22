package com.noc.dashboardservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * Feign client into alarm-service, resolved by its Eureka-registered
 * application name ("alarm-service") — no hardcoded host/port, per the
 * cross-service communication rule.
 */
@FeignClient(name = "alarm-service")
public interface AlarmClient {

    /** All alarms, optionally filtered by severity/status/device — filtering client-side is left to callers. */
    @GetMapping("/api/alarms")
    List<AlarmResponse> getAlarms(
            @RequestParam(value = "severity", required = false) String severity,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "deviceId", required = false) String deviceId);
}
