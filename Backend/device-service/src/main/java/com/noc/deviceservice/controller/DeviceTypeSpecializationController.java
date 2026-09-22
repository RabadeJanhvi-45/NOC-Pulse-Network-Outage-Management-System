package com.noc.deviceservice.controller;

import com.noc.deviceservice.dto.DeviceTypeSpecializationRequest;
import com.noc.deviceservice.dto.DeviceTypeSpecializationResponse;
import com.noc.deviceservice.exception.ResourceNotFoundException;
import com.noc.deviceservice.service.DeviceTypeSpecializationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Reachable through api-gateway at /api/device-type-specializations/**.
 * CRUD is ADMIN-only (see SecurityConfig). The /by-type/{deviceType}
 * lookup is the internal-facing endpoint incident-service's assignment
 * engine calls via Feign — left reachable to any authenticated caller,
 * same convention as auth-service's internal endpoints.
 */
@RestController
@RequestMapping("/api/device-type-specializations")
@RequiredArgsConstructor
public class DeviceTypeSpecializationController {

    private final DeviceTypeSpecializationService service;

    @PostMapping
    public ResponseEntity<DeviceTypeSpecializationResponse> create(
            @Valid @RequestBody DeviceTypeSpecializationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }

    @GetMapping
    public ResponseEntity<List<DeviceTypeSpecializationResponse>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    @PutMapping("/{id}")
    public ResponseEntity<DeviceTypeSpecializationResponse> update(
            @PathVariable Long id, @Valid @RequestBody DeviceTypeSpecializationRequest request) {
        return ResponseEntity.ok(service.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/by-type/{deviceType}")
    public ResponseEntity<DeviceTypeSpecializationResponse> findByDeviceType(@PathVariable String deviceType) {
        return service.findByDeviceType(deviceType)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No specialization mapping found for device type: " + deviceType));
    }
}