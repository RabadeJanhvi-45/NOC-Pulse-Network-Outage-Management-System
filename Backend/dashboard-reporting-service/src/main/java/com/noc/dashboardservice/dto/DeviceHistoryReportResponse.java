package com.noc.dashboardservice.dto;

import com.noc.dashboardservice.client.AlarmResponse;
import com.noc.dashboardservice.client.IncidentResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/** Combined alarm + incident history for a single device (Backend Guide §6.1). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeviceHistoryReportResponse {

    private String deviceId;
    private String deviceName;
    private String deviceStatus;

    private List<AlarmResponse> alarms;
    private List<IncidentResponse> incidents;
}
