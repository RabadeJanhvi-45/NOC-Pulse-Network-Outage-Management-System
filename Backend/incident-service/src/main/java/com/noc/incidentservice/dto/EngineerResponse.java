package com.noc.incidentservice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Client-side view of auth-service's EngineerResponse (GET /api/engineers),
 * consumed by the assignment engine. Extra fields on the real response are
 * ignored during deserialization.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EngineerResponse {

    private Long userId;
    private String username;
    private String fullName;
    private Boolean enabled;
    private String primarySpecialization;
    private Integer activeTaskLimit;
    private List<String> skills;
}