package com.noc.dashboardservice.controller;

import com.noc.dashboardservice.dto.DashboardSummaryResponse;
import com.noc.dashboardservice.dto.GroupedCountResponse;
import com.noc.dashboardservice.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST endpoints for dashboard widgets (Backend Guide §6.1).
 * Reachable through api-gateway at /api/dashboard/**.
 * Every response is computed live from device-service, alarm-service and
 * incident-service — nothing here is persisted.
 */
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    /** Total devices, active alarms, open incidents. */
    @GetMapping("/summary")
    public ResponseEntity<DashboardSummaryResponse> getSummary() {
        return ResponseEntity.ok(dashboardService.getSummary());
    }

    /** Alarm counts grouped by severity, for widget charts. */
    @GetMapping("/alarms-by-severity")
    public ResponseEntity<GroupedCountResponse> getAlarmsBySeverity() {
        return ResponseEntity.ok(dashboardService.getAlarmsBySeverity());
    }

    /** Incident counts grouped by status, for widget charts. */
    @GetMapping("/incidents-by-status")
    public ResponseEntity<GroupedCountResponse> getIncidentsByStatus() {
        return ResponseEntity.ok(dashboardService.getIncidentsByStatus());
    }
}
