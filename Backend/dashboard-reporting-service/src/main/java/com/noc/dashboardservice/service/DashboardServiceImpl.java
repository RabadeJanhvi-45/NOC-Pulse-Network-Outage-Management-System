package com.noc.dashboardservice.service;

import com.noc.dashboardservice.client.AlarmClient;
import com.noc.dashboardservice.client.AlarmResponse;
import com.noc.dashboardservice.client.DeviceClient;
import com.noc.dashboardservice.client.IncidentClient;
import com.noc.dashboardservice.client.IncidentResponse;
import com.noc.dashboardservice.dto.DashboardSummaryResponse;
import com.noc.dashboardservice.dto.GroupedCountResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    /** Alarm statuses that count as "active" for the summary widget (alarm-service: Active/Acknowledged/Cleared). */
    private static final Set<String> ACTIVE_ALARM_STATUSES = Set.of("ACTIVE", "ACKNOWLEDGED");

    /** Incident statuses that count as "open" for the summary widget (incident-service: NEW/ASSIGNED/IN_PROGRESS/RESOLVED/CLOSED). */
    private static final Set<String> OPEN_INCIDENT_STATUSES = Set.of("NEW", "ASSIGNED", "IN_PROGRESS", "OPEN", "IN PROGRESS");

    private final DeviceClient deviceClient;
    private final AlarmClient alarmClient;
    private final IncidentClient incidentClient;

    @Override
    public DashboardSummaryResponse getSummary() {
        long totalDevices = deviceClient.getDevices(null, null, null, null).size();

        long activeAlarms = alarmClient.getAlarms(null, null, null).stream()
                .filter(a -> a.getStatus() != null && ACTIVE_ALARM_STATUSES.contains(a.getStatus().trim().toUpperCase()))
                .count();

        long openIncidents = incidentClient.getIncidents(null, null).stream()
                .filter(i -> i.getStatus() != null && OPEN_INCIDENT_STATUSES.contains(i.getStatus().trim().toUpperCase()))
                .count();

        return DashboardSummaryResponse.builder()
                .totalDevices(totalDevices)
                .activeAlarms(activeAlarms)
                .openIncidents(openIncidents)
                .generatedAt(LocalDateTime.now())
                .build();
    }

    @Override
    public GroupedCountResponse getAlarmsBySeverity() {
        List<AlarmResponse> alarms = alarmClient.getAlarms(null, null, null);
        var counts = alarms.stream()
                .collect(Collectors.groupingBy(AlarmResponse::getSeverity, Collectors.counting()));

        return GroupedCountResponse.builder()
                .groupedBy("severity")
                .counts(counts)
                .total(alarms.size())
                .build();
    }

    @Override
    public GroupedCountResponse getIncidentsByStatus() {
        List<IncidentResponse> incidents = incidentClient.getIncidents(null, null);
        var counts = incidents.stream()
                .collect(Collectors.groupingBy(IncidentResponse::getStatus, Collectors.counting()));

        return GroupedCountResponse.builder()
                .groupedBy("status")
                .counts(counts)
                .total(incidents.size())
                .build();
    }
}
