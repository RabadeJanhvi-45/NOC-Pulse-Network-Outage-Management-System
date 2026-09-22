package com.noc.dashboardservice.service;

import com.noc.dashboardservice.client.AlarmClient;
import com.noc.dashboardservice.client.AlarmResponse;
import com.noc.dashboardservice.client.DeviceClient;
import com.noc.dashboardservice.client.DeviceResponse;
import com.noc.dashboardservice.client.IncidentClient;
import com.noc.dashboardservice.client.IncidentResponse;
import com.noc.dashboardservice.dto.DeviceHistoryReportResponse;
import com.noc.dashboardservice.dto.OutageReportResponse;
import com.noc.dashboardservice.exception.ResourceNotFoundException;
import com.noc.dashboardservice.exception.UpstreamServiceUnavailableException;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private static final Set<String> CLOSED_INCIDENT_STATUSES = Set.of("Resolved", "Closed");

    private final DeviceClient deviceClient;
    private final AlarmClient alarmClient;
    private final IncidentClient incidentClient;

    /**
     * Neither alarm-service nor incident-service expose a date-range filter, so the
     * full list is fetched and filtered client-side (guide §6.2 — kept stateless,
     * acceptable for MVP report volume).
     */
    @Override
    public OutageReportResponse getOutageSummary(LocalDateTime from, LocalDateTime to) {
        if (from == null || to == null) {
            throw new IllegalArgumentException("Both 'from' and 'to' query parameters are required");
        }
        if (from.isAfter(to)) {
            throw new IllegalArgumentException("'from' must not be after 'to'");
        }

        List<AlarmResponse> alarmsInRange = alarmClient.getAlarms(null, null, null).stream()
                .filter(a -> inRange(a.getRaisedAt(), from, to))
                .toList();

        List<IncidentResponse> incidentsInRange = incidentClient.getIncidents(null, null).stream()
                .filter(i -> inRange(i.getCreatedAt(), from, to))
                .toList();

        var alarmsBySeverity = alarmsInRange.stream()
                .collect(Collectors.groupingBy(AlarmResponse::getSeverity, Collectors.counting()));

        var incidentsByStatus = incidentsInRange.stream()
                .collect(Collectors.groupingBy(IncidentResponse::getStatus, Collectors.counting()));

        long incidentsResolved = incidentsInRange.stream()
                .filter(i -> CLOSED_INCIDENT_STATUSES.contains(i.getStatus()))
                .count();

        return OutageReportResponse.builder()
                .from(from)
                .to(to)
                .totalAlarmsRaised(alarmsInRange.size())
                .alarmsBySeverity(alarmsBySeverity)
                .totalIncidentsCreated(incidentsInRange.size())
                .incidentsResolved(incidentsResolved)
                .incidentsByStatus(incidentsByStatus)
                .generatedAt(LocalDateTime.now())
                .build();
    }

    @Override
    public DeviceHistoryReportResponse getDeviceHistory(String deviceId) {
        DeviceResponse device;
        try {
            device = deviceClient.getByDeviceId(deviceId);
        } catch (FeignException.NotFound ex) {
            throw new ResourceNotFoundException("Device not found: " + deviceId);
        } catch (FeignException ex) {
            throw new UpstreamServiceUnavailableException(
                    "Could not reach device-service to fetch deviceId '" + deviceId + "': " + ex.getMessage());
        }

        List<AlarmResponse> alarms = alarmClient.getAlarms(null, null, deviceId);

        // incident-service has no deviceId filter — fetch all and filter client-side.
        List<IncidentResponse> incidents = incidentClient.getIncidents(null, null).stream()
                .filter(i -> deviceId.equals(i.getDeviceId()))
                .toList();

        return DeviceHistoryReportResponse.builder()
                .deviceId(device.getDeviceId())
                .deviceName(device.getName())
                .deviceStatus(device.getStatus())
                .alarms(alarms)
                .incidents(incidents)
                .build();
    }

    private boolean inRange(LocalDateTime timestamp, LocalDateTime from, LocalDateTime to) {
        return timestamp != null && !timestamp.isBefore(from) && !timestamp.isAfter(to);
    }
}
