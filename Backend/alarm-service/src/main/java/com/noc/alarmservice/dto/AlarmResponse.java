package com.noc.alarmservice.dto;

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
public class AlarmResponse {

    private Long id;
    private String deviceId;
    private String alarmType;
    private String severity;
    private String status;
    private String groupKey;
    private String raisedBy;
    private LocalDateTime raisedAt;

    /** True when this call returned an existing Active alarm instead of creating a new one (dedup). */
    private boolean deduplicated;
}
