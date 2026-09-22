package com.noc.incidentservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Body for auth-service's POST /api/notifications/internal. Either userId
 * or role must be given: userId targets one user directly; role broadcasts
 * to everyone holding that role (e.g. "ADMIN").
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateNotificationRequest {

    private Long userId;
    private String role;
    private String type;
    private String message;
}