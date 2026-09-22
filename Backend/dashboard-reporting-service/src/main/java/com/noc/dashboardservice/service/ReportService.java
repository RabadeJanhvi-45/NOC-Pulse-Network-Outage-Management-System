package com.noc.dashboardservice.service;

import com.noc.dashboardservice.dto.DeviceHistoryReportResponse;
import com.noc.dashboardservice.dto.OutageReportResponse;

import java.time.LocalDateTime;

public interface ReportService {

    OutageReportResponse getOutageSummary(LocalDateTime from, LocalDateTime to);

    DeviceHistoryReportResponse getDeviceHistory(String deviceId);
}
