package com.noc.dashboardservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/** Top-line counts for the dashboard landing widget. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardSummaryResponse {

    private long totalDevices;

    /** Alarms with status Active or Acknowledged (not yet Cleared). */
    private long activeAlarms;

    /** Incidents with status Open or In Progress (not yet Resolved/Closed). */
    private long openIncidents;

    private LocalDateTime generatedAt;
}
