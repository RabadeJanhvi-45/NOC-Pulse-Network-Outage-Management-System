package com.noc.authservice.controller;

import com.noc.authservice.dto.CreateNotificationRequest;
import com.noc.authservice.dto.NotificationResponse;
import com.noc.authservice.service.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Reachable through api-gateway at /api/notifications/**.
 * GET/POST-read are own-notifications-only, identified from the validated
 * JWT (SecurityContext) — never from a client-supplied header.
 * POST /api/notifications/internal is the Feign-facing endpoint other
 * services call to push a notification — left reachable to any
 * authenticated caller (all callers in this project's scope are trusted
 * services), see SecurityConfig.
 */
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<List<NotificationResponse>> getMyNotifications(Authentication authentication) {
        return ResponseEntity.ok(notificationService.getMyNotifications(currentUserId(authentication)));
    }

    @PostMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(
            @PathVariable Long id, Authentication authentication) {
        notificationService.markAsRead(id, currentUserId(authentication));
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/internal")
    public ResponseEntity<Void> create(@Valid @RequestBody CreateNotificationRequest request) {
        notificationService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    private Long currentUserId(Authentication authentication) {
        return (Long) authentication.getDetails();
    }
}