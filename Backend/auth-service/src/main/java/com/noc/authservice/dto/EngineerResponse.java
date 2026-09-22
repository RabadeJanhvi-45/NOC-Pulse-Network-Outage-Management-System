package com.noc.authservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/** Shape consumed by incident-service's assignment engine via Feign. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EngineerResponse {

    private Long userId;
    private String username;
    private String fullName;
    private Boolean enabled;
    private String primarySpecialization;
    private Integer activeTaskLimit;
    private List<String> skills;
}