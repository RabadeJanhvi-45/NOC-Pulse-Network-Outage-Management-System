package com.noc.dashboardservice.service;

import com.noc.dashboardservice.dto.DashboardSummaryResponse;
import com.noc.dashboardservice.dto.GroupedCountResponse;

public interface DashboardService {

    DashboardSummaryResponse getSummary();

    GroupedCountResponse getAlarmsBySeverity();

    GroupedCountResponse getIncidentsByStatus();
}
