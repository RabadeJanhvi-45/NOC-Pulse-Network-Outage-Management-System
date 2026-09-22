package com.noc.incidentservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Body for endpoints requiring a mandatory reason: reject-assignment, reject-resolution. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReasonRequest {

    @NotBlank(message = "reason is required")
    private String reason;
}