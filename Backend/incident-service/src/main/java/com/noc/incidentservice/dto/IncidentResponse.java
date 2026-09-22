package com.noc.incidentservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IncidentResponse {

    private Long id;
    private String deviceId;
    private Long alarmId;
    private String description;
    private String priority;
    private String status;
    private String resolutionNotes;
    private String createdBy;
    private String assigneeId;
    private String assignedEngineer;
    private String assignedEngineerUsername;
    private String assignedEngineerSpecialization;
    private LocalDateTime createdAt;
    private LocalDateTime closedAt;
    private String closedBy;
}
