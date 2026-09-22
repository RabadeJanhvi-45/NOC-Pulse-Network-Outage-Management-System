package com.noc.dashboardservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * Feign client into incident-service, resolved by its Eureka-registered
 * application name ("incident-service") — no hardcoded host/port, per the
 * cross-service communication rule.
 *
 * incident-service only exposes status/priority filters (no deviceId
 * filter), so per-device reports fetch the full list and filter by
 * deviceId client-side — acceptable since this service is stateless and
 * incident volume is expected to be modest (Backend Guide §6.2).
 */
@FeignClient(name = "incident-service")
public interface IncidentClient {

    @GetMapping("/api/incidents")
    List<IncidentResponse> getIncidents(
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "priority", required = false) String priority);
}
