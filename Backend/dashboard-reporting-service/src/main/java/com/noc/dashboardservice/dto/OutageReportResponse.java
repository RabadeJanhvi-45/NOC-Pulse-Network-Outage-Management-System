package com.noc.dashboardservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Map;

/** Outage report generated on-the-fly over a date range (Backend Guide §6.1). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OutageReportResponse {

    private LocalDateTime from;
    private LocalDateTime to;

    private long totalAlarmsRaised;
    private Map<String, Long> alarmsBySeverity;

    private long totalIncidentsCreated;
    private long incidentsResolved;
    private Map<String, Long> incidentsByStatus;

    private LocalDateTime generatedAt;
}
