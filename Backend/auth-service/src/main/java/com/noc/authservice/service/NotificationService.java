package com.noc.authservice.service;

import com.noc.authservice.dto.CreateNotificationRequest;
import com.noc.authservice.dto.NotificationResponse;

import java.util.List;

public interface NotificationService {

    List<NotificationResponse> getMyNotifications(Long userId);

    void markAsRead(Long id, Long userId);

    /** Used by both other services (via the internal endpoint) and auth-service itself (e.g. RegistrationServiceImpl). */
    void notifyUser(Long userId, String type, String message);

    void notifyRole(String role, String type, String message);

    void create(CreateNotificationRequest request);
}