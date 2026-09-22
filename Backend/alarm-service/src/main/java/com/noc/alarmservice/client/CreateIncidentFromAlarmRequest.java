package com.noc.alarmservice.client;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Body sent to incident-service's POST /api/incidents/internal/from-alarm. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateIncidentFromAlarmRequest {
    private String deviceId;
    private Long alarmId;
    /** P1 (Critical) / P2 (Major) / P3 (Minor) / P4 (Warning) — mapped from the alarm's severity. */
    private String priority;
    private String description;
    private String raisedBy;
}