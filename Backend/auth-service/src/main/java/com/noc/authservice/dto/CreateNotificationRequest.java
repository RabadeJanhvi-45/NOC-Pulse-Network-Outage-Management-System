package com.noc.authservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Body for POST /api/notifications/internal. Either userId or role must be
 * given: userId targets one user directly; role broadcasts one Notification
 * row per user currently holding that role (e.g. "ADMIN" for every Admin).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateNotificationRequest {

    private Long userId;

    private String role;

    @NotBlank(message = "type is required")
    private String type;

    @NotBlank(message = "message is required")
    private String message;
}