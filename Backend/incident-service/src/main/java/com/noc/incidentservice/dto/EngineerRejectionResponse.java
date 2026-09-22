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
public class EngineerRejectionResponse {

    private Long id;
    private Long incidentId;
    private String engineerId;
    private String reason;
    private String adminDecision;
    private String decidedBy;
    private LocalDateTime rejectedAt;
    private LocalDateTime decidedAt;
}