package com.noc.dashboardservice.controller;

import com.noc.dashboardservice.dto.DeviceHistoryReportResponse;
import com.noc.dashboardservice.dto.OutageReportResponse;
import com.noc.dashboardservice.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

/**
 * REST endpoints for on-the-fly reports (Backend Guide §6.1).
 * Reachable through api-gateway at /api/reports/**.
 * Reports are generated fresh on every call — no persistence for MVP
 * (guide §6.2 notes a report-db could be added later if saving generated
 * reports is required).
 */
@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    /** Outage summary over a date range: alarm/incident volume and breakdowns. */
    @GetMapping("/outage-summary")
    public ResponseEntity<OutageReportResponse> getOutageSummary(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        return ResponseEntity.ok(reportService.getOutageSummary(from, to));
    }

    /** Combined alarm + incident history for one device, keyed by its business deviceId. */
    @GetMapping("/device/{deviceId}/history")
    public ResponseEntity<DeviceHistoryReportResponse> getDeviceHistory(@PathVariable String deviceId) {
        return ResponseEntity.ok(reportService.getDeviceHistory(deviceId));
    }
}
