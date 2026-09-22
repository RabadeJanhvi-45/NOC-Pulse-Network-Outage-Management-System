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
public class EscalationResponse {

    private Long id;
    private Long incidentId;
    private String escalatedBy;
    private String reason;
    private String triggerType;
    private LocalDateTime escalatedAt;
}
