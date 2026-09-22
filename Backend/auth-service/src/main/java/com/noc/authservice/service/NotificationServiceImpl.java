package com.noc.authservice.service;

import com.noc.authservice.dto.CreateNotificationRequest;
import com.noc.authservice.dto.NotificationResponse;
import com.noc.authservice.entity.Notification;
import com.noc.authservice.entity.Role;
import com.noc.authservice.entity.User;
import com.noc.authservice.exception.ResourceNotFoundException;
import com.noc.authservice.repository.NotificationRepository;
import com.noc.authservice.repository.RoleRepository;
import com.noc.authservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @Override
    public List<NotificationResponse> getMyNotifications(Long userId) {
        return notificationRepository.findByUserId(userId).stream()
                .sorted(Comparator.comparing(Notification::getCreatedAt).reversed())
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public void markAsRead(Long id, Long userId) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with id: " + id));
        if (!notification.getUserId().equals(userId)) {
            throw new IllegalArgumentException("Notification " + id + " does not belong to this user");
        }
        notification.setIsRead(true);
        notificationRepository.save(notification);
    }

    @Override
    @Transactional
    public void notifyUser(Long userId, String type, String message) {
        notificationRepository.save(Notification.builder()
                .userId(userId)
                .type(type)
                .message(message)
                .build());
    }

    @Override
    @Transactional
    public void notifyRole(String role, String type, String message) {
        Role targetRole = roleRepository.findByNameIgnoreCase(role)
                .orElseThrow(() -> new IllegalStateException(role + " role is not seeded"));

        List<User> recipients = userRepository.findAll().stream()
                .filter(u -> targetRole.getId().equals(u.getRoleId()))
                .toList();

        for (User recipient : recipients) {
            notifyUser(recipient.getId(), type, message);
        }
    }

    @Override
    @Transactional
    public void create(CreateNotificationRequest request) {
        if (request.getUserId() != null) {
            notifyUser(request.getUserId(), request.getType(), request.getMessage());
        } else if (StringUtils.hasText(request.getRole())) {
            notifyRole(request.getRole(), request.getType(), request.getMessage());
        } else {
            throw new IllegalArgumentException("Either userId or role is required");
        }
    }

    // ---- helpers ----

    private NotificationResponse toResponse(Notification n) {
        return NotificationResponse.builder()
                .id(n.getId())
                .type(n.getType())
                .message(n.getMessage())
                .isRead(n.getIsRead())
                .createdAt(n.getCreatedAt())
                .build();
    }
}