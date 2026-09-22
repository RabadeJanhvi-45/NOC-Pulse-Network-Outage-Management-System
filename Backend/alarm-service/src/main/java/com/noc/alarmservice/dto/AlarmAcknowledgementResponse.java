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
public class AlarmAcknowledgementResponse {

    private Long id;
    private Long alarmId;
    private String acknowledgedBy;
    private LocalDateTime acknowledgedAt;
}
