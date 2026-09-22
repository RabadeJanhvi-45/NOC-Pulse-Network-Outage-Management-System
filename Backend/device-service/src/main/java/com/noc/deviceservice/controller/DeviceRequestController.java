package com.noc.deviceservice.controller;

import com.noc.deviceservice.dto.*;
import com.noc.deviceservice.service.DeviceRequestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Reachable through api-gateway at /api/device-requests/**.
 * POST is NOC_OPERATOR; everything else is ADMIN-only (see SecurityConfig).
 */
@RestController
@RequestMapping("/api/device-requests")
@RequiredArgsConstructor
public class DeviceRequestController {

    private final DeviceRequestService deviceRequestService;

    @PostMapping
    public ResponseEntity<DeviceRegistrationRequestResponse> submit(
            @Valid @RequestBody DeviceRequestSubmission submission,
            @RequestHeader(value = "X-User-Id", required = false) String requestedBy) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(deviceRequestService.submitNewDeviceRequest(submission, requestedBy));
    }

    @GetMapping
    public ResponseEntity<List<DeviceRegistrationRequestResponse>> getRequests() {
        return ResponseEntity.ok(deviceRequestService.getRequests());
    }

        @PostMapping("/{id}/approve")
    public ResponseEntity<DeviceResponse> approve(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Id", required = false) String reviewedBy) {
        return ResponseEntity.ok(deviceRequestService.approve(id, reviewedBy));
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<DeviceRegistrationRequestResponse> reject(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Id", required = false) String reviewedBy,
            @Valid @RequestBody DeviceRejectRequest request) {
        return ResponseEntity.ok(deviceRequestService.rejectRequest(id, reviewedBy, request.getReason()));
    }
}