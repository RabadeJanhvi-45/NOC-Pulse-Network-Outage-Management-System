package com.noc.deviceservice.controller;

import com.noc.deviceservice.dto.DeviceActiveResponse;
import com.noc.deviceservice.dto.DeviceHistoryResponse;
import com.noc.deviceservice.dto.DeviceRequest;
import com.noc.deviceservice.dto.DeviceResponse;
import com.noc.deviceservice.service.DeviceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST endpoints for device-service (US-04, US-05, US-06).
 * Reachable through api-gateway at /api/devices/**.
 */
@RestController
@RequestMapping("/api/devices")
@RequiredArgsConstructor
public class DeviceController {

    private final DeviceService deviceService;

    /** US-04: Device Registration. */
    @PostMapping
    public ResponseEntity<DeviceResponse> registerDevice(
            @Valid @RequestBody DeviceRequest request,
            org.springframework.security.core.Authentication authentication,
            @RequestHeader(value = "X-Username", required = false) String username,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        String changedBy = resolveUsername(authentication, username, userId);
        DeviceResponse response = deviceService.registerDevice(request, changedBy);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /** US-06: View Device Inventory — supports filtering by region/status/type and creator. */
    @GetMapping
    public ResponseEntity<List<DeviceResponse>> getDevices(
            @RequestParam(required = false) String region,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String deviceType,
            @RequestParam(required = false) String search,
            org.springframework.security.core.Authentication authentication,
            @RequestHeader(value = "X-Role", required = false) String callerRole,
            @RequestHeader(value = "X-Username", required = false) String callerUsername,
            @RequestHeader(value = "X-User-Id", required = false) String callerUserId) {
        String role = resolveRole(authentication, callerRole);
        String username = resolveUsername(authentication, callerUsername, callerUserId);
        return ResponseEntity.ok(deviceService.getDevices(region, status, deviceType, search, role, username, callerUserId));
    }

    /** US-06: device detail profile. */
    @GetMapping("/{id}")
    public ResponseEntity<DeviceResponse> getDeviceById(@PathVariable Long id) {
        return ResponseEntity.ok(deviceService.getDeviceById(id));
    }

    /**
     * Lookup by business deviceId (not the DB primary key) — used by alarm-service /
     * incident-service via Feign to confirm a deviceId exists before saving.
     */
        @GetMapping("/by-device-id/{deviceId}")
    public ResponseEntity<DeviceResponse> getDeviceByBusinessId(@PathVariable String deviceId) {
        return ResponseEntity.ok(deviceService.getDeviceByBusinessId(deviceId));
    }

    /**
     * Internal-facing: alarm-service calls this via Feign before saving a
     * new Alarm, to reject alarm creation if the device isn't Active. A
     * deviceId that doesn't exist at all is treated as not-active (false),
     * not a 404 — alarm-service just needs a yes/no.
     */
    @GetMapping("/by-device-id/{deviceId}/active-check")
    public ResponseEntity<DeviceActiveResponse> checkDeviceActive(@PathVariable String deviceId) {
        boolean active = deviceService.isDeviceActive(deviceId);
        return ResponseEntity.ok(DeviceActiveResponse.builder().deviceId(deviceId).active(active).build());
    }

    /** US-05: Edit Device Details — writes a DeviceHistory entry per changed field. */
    @PutMapping("/{id}")
    public ResponseEntity<DeviceResponse> updateDevice(
            @PathVariable Long id,
            @Valid @RequestBody DeviceRequest request,
            org.springframework.security.core.Authentication authentication,
            @RequestHeader(value = "X-Username", required = false) String username,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        String changedBy = resolveUsername(authentication, username, userId);
        return ResponseEntity.ok(deviceService.updateDevice(id, request, changedBy));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDevice(@PathVariable Long id) {
        deviceService.deleteDevice(id);
        return ResponseEntity.noContent().build();
    }

    /** View change history for a device. */
    @GetMapping("/{id}/history")
    public ResponseEntity<List<DeviceHistoryResponse>> getHistory(@PathVariable Long id) {
        return ResponseEntity.ok(deviceService.getHistory(id));
    }

    private String resolveUsername(org.springframework.security.core.Authentication auth, String headerUsername, String headerUserId) {
        if (auth != null && org.springframework.util.StringUtils.hasText(auth.getName())) {
            return auth.getName();
        }
        if (org.springframework.util.StringUtils.hasText(headerUsername)) {
            return headerUsername;
        }
        return headerUserId;
    }

    private String resolveRole(org.springframework.security.core.Authentication auth, String headerRole) {
        if (auth != null && auth.getAuthorities() != null && !auth.getAuthorities().isEmpty()) {
            return auth.getAuthorities().iterator().next().getAuthority().replace("ROLE_", "");
        }
        return headerRole;
    }
}
