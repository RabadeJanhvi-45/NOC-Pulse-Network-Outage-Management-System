package com.noc.deviceservice.service;

import com.noc.deviceservice.client.NotificationServiceClient;
import com.noc.deviceservice.dto.*;
import com.noc.deviceservice.entity.DeviceRegistrationRequest;
import com.noc.deviceservice.exception.DuplicateDeviceException;
import com.noc.deviceservice.exception.ResourceNotFoundException;
import com.noc.deviceservice.repository.DeviceRegistrationRequestRepository;
import com.noc.deviceservice.repository.DeviceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DeviceRequestServiceImpl implements DeviceRequestService {

    private static final String NEW = "NEW";
    private static final String FIELD_CHANGE = "FIELD_CHANGE";
    private static final String DEACTIVATION = "DEACTIVATION";
    private static final String PENDING = "PENDING";
    private static final String APPROVED = "APPROVED";
    private static final String REJECTED = "REJECTED";

    private final DeviceRegistrationRequestRepository requestRepository;
    private final DeviceRepository deviceRepository;
    private final DeviceService deviceService;
    private final NotificationServiceClient notificationServiceClient;

    @Override
    @Transactional
    public DeviceRegistrationRequestResponse submitNewDeviceRequest(DeviceRequestSubmission submission, String requestedBy) {
        if (deviceRepository.existsByDeviceIdIgnoreCase(submission.getDeviceId())) {
            throw new DuplicateDeviceException(
                    "A device with deviceId '" + submission.getDeviceId() + "' already exists");
        }

        DeviceRegistrationRequest request = DeviceRegistrationRequest.builder()
                .requestType(NEW)
                .deviceId(submission.getDeviceId())
                .deviceType(submission.getDeviceType())
                .name(submission.getName())
                .ipAddress(submission.getIpAddress())
                .location(submission.getLocation())
                .region(submission.getRegion())
                .requestedBy(requestedBy)
                .status(PENDING)
                .build();

        DeviceRegistrationRequest saved = requestRepository.save(request);
        notifyRole("ADMIN", "DEVICE_REQUEST", "New device registration request for '" + saved.getDeviceId() + "'.");
        return toResponse(saved);
    }

    @Override
    public List<DeviceRegistrationRequestResponse> getRequests() {
        return requestRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional
    public DeviceResponse approve(Long id, String reviewedBy) {
        DeviceRegistrationRequest request = findRequestOrThrow(id);
        requirePending(request);

        DeviceResponse result = switch (request.getRequestType()) {
            case NEW -> approveNew(request, reviewedBy);
            case FIELD_CHANGE -> deviceService.applyApprovedFieldChange(
                    request.getTargetDeviceId(), request.getDeviceType(), request.getIpAddress(), reviewedBy);
            case DEACTIVATION -> deviceService.applyApprovedDeactivation(request.getTargetDeviceId(), reviewedBy);
            default -> throw new IllegalStateException("Unknown requestType: " + request.getRequestType());
        };

        request.setStatus(APPROVED);
        request.setReviewedBy(reviewedBy);
        request.setReviewedAt(LocalDateTime.now());
        requestRepository.save(request);

        if (StringUtils.hasText(request.getRequestedBy())) {
            notifyUser(request.getRequestedBy(), "DEVICE_REQUEST_DECISION",
                "Your device request for '" + request.getDeviceId() + "' was approved.");
        }

        return result;
    }

    @Override
    @Transactional
    public DeviceRegistrationRequestResponse rejectRequest(Long id, String reviewedBy, String reason) {
        DeviceRegistrationRequest request = findRequestOrThrow(id);
        requirePending(request);

        request.setStatus(REJECTED);
        request.setRejectionReason(reason);
        request.setReviewedBy(reviewedBy);
        request.setReviewedAt(LocalDateTime.now());
        DeviceRegistrationRequest saved = requestRepository.save(request);

        if (StringUtils.hasText(request.getRequestedBy())) {
            notifyUser(request.getRequestedBy(), "DEVICE_REQUEST_DECISION",
                "Your device request for '" + request.getDeviceId() + "' was rejected: " + reason);
        }

        return toResponse(saved);
    }

    // ---- helpers ----

    private DeviceResponse approveNew(DeviceRegistrationRequest request, String reviewedBy) {
        DeviceRequest deviceRequest = DeviceRequest.builder()
                .deviceId(request.getDeviceId())
                .deviceType(request.getDeviceType())
                .name(request.getName())
                .ipAddress(request.getIpAddress())
                .location(request.getLocation())
                .region(request.getRegion())
                .build();
        return deviceService.registerDevice(deviceRequest, reviewedBy);
    }

    private DeviceRegistrationRequest findRequestOrThrow(Long id) {
        return requestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Device request not found with id: " + id));
    }

    private void requirePending(DeviceRegistrationRequest request) {
        if (!PENDING.equalsIgnoreCase(request.getStatus())) {
            throw new IllegalStateException("Device request has already been reviewed");
        }
    }

    private DeviceRegistrationRequestResponse toResponse(DeviceRegistrationRequest r) {
        return DeviceRegistrationRequestResponse.builder()
                .id(r.getId())
                .requestType(r.getRequestType())
                .targetDeviceId(r.getTargetDeviceId())
                .deviceId(r.getDeviceId())
                .deviceType(r.getDeviceType())
                .name(r.getName())
                .ipAddress(r.getIpAddress())
                .location(r.getLocation())
                .region(r.getRegion())
                .requestedBy(r.getRequestedBy())
                .status(r.getStatus())
                .rejectionReason(r.getRejectionReason())
                .reviewedBy(r.getReviewedBy())
                .requestedAt(r.getRequestedAt())
                .reviewedAt(r.getReviewedAt())
                .build();
    }

    private void notifyUser(String userId, String type, String message) {
        notificationServiceClient.create(CreateNotificationRequest.builder()
                .userId(Long.valueOf(userId)).type(type).message(message).build());
    }

    private void notifyRole(String role, String type, String message) {
        notificationServiceClient.create(CreateNotificationRequest.builder()
                .role(role).type(type).message(message).build());
    }
}