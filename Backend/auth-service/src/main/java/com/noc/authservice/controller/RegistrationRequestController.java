package com.noc.authservice.controller;

import com.noc.authservice.dto.RegisterRequest;
import com.noc.authservice.dto.RegistrationRequestResponse;
import com.noc.authservice.dto.RejectRequest;
import com.noc.authservice.service.RegistrationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * POST /api/auth/register is public (permitAll — see SecurityConfig).
 * Everything under /api/registration-requests/** is ADMIN-only.
 */
@RestController
@RequiredArgsConstructor
public class RegistrationRequestController {

    private final RegistrationService registrationService;

    @PostMapping("/api/auth/register")
    public ResponseEntity<RegistrationRequestResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(registrationService.register(request));
    }

    @PostMapping("/api/auth/register/engineer")
    public ResponseEntity<RegistrationRequestResponse> registerEngineer(@Valid @RequestBody com.noc.authservice.dto.EngineerRegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(registrationService.registerEngineer(request));
    }

    @GetMapping("/api/registration-requests")
    public ResponseEntity<List<RegistrationRequestResponse>> getRequests() {
        return ResponseEntity.ok(registrationService.getRequests());
    }

    @PostMapping("/api/registration-requests/{id}/approve")
    public ResponseEntity<RegistrationRequestResponse> approve(
            @PathVariable Long id, @RequestHeader(value = "X-User-Id", required = false) Long reviewerId) {
        Long reviewer = reviewerId != null ? reviewerId : 1L;
        return ResponseEntity.ok(registrationService.approve(id, reviewer));
    }

    @PostMapping("/api/registration-requests/{id}/reject")
    public ResponseEntity<RegistrationRequestResponse> reject(
            @PathVariable Long id, @RequestHeader(value = "X-User-Id", required = false) Long reviewerId,
            @Valid @RequestBody RejectRequest request) {
        Long reviewer = reviewerId != null ? reviewerId : 1L;
        return ResponseEntity.ok(registrationService.reject(id, reviewer, request.getReason()));
    }
}