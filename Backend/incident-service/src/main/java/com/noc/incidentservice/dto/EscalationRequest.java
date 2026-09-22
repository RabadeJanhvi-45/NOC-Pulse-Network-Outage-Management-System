package com.noc.incidentservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EscalationRequest {

    @NotBlank
    private String escalatedBy;

    @NotBlank
    private String reason;
}
