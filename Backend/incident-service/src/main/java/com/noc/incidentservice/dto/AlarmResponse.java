package com.noc.incidentservice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Client-side view of alarm-service's AlarmResponse — only the fields
 * incident-service actually needs from GET /api/alarms/{id}.
 * Extra fields in the real response are simply ignored during deserialization.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AlarmResponse {

    private Long id;
    private String deviceId;
    private String alarmType;
    private String severity;
    private String status;
}
