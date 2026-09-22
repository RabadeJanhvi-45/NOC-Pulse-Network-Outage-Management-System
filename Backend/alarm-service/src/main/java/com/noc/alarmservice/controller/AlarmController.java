package com.noc.alarmservice.controller;

import com.noc.alarmservice.dto.AlarmRequest;
import com.noc.alarmservice.dto.AlarmResponse;
import com.noc.alarmservice.dto.SeverityRuleRequest;
import com.noc.alarmservice.dto.SeverityRuleResponse;
import com.noc.alarmservice.service.AlarmService;
import com.noc.alarmservice.service.SeverityRuleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST endpoints for alarm-service.
 * Reachable through api-gateway at /api/alarms/**.
 */
@RestController
@RequestMapping("/api/alarms")
@RequiredArgsConstructor
public class AlarmController {

    private final AlarmService alarmService;
    private final SeverityRuleService severityRuleService;

    /** Raise a new alarm. Auto-classifies severity via SeverityRule if not supplied, and dedupes by groupKey. */
    @PostMapping
    public ResponseEntity<AlarmResponse> raiseAlarm(
            @Valid @RequestBody AlarmRequest request,
            org.springframework.security.core.Authentication authentication,
            @RequestHeader(value = "X-Username", required = false) String headerUsername) {
        String callerUsername = resolveUsername(authentication, headerUsername);
        AlarmResponse response = alarmService.raiseAlarm(request, callerUsername);
        HttpStatus status = response.isDeduplicated() ? HttpStatus.OK : HttpStatus.CREATED;
        return ResponseEntity.status(status).body(response);
    }

    /** List alarms, optionally filtered by severity/status/device and creator. */
    @GetMapping
    public ResponseEntity<List<AlarmResponse>> getAlarms(
            @RequestParam(required = false) String severity,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String deviceId,
            org.springframework.security.core.Authentication authentication,
            @RequestHeader(value = "X-Role", required = false) String headerRole,
            @RequestHeader(value = "X-Username", required = false) String headerUsername) {
        String role = resolveRole(authentication, headerRole);
        String username = resolveUsername(authentication, headerUsername);
        return ResponseEntity.ok(alarmService.getAlarms(severity, status, deviceId, role, username));
    }

    private String resolveUsername(org.springframework.security.core.Authentication auth, String headerUsername) {
        if (auth != null && org.springframework.util.StringUtils.hasText(auth.getName())) {
            return auth.getName();
        }
        return headerUsername;
    }

    private String resolveRole(org.springframework.security.core.Authentication auth, String headerRole) {
        if (auth != null && auth.getAuthorities() != null && !auth.getAuthorities().isEmpty()) {
            return auth.getAuthorities().iterator().next().getAuthority().replace("ROLE_", "");
        }
        return headerRole;
    }

    /** Alarm detail. */
    @GetMapping("/{id}")
    public ResponseEntity<AlarmResponse> getAlarmById(@PathVariable Long id) {
        return ResponseEntity.ok(alarmService.getAlarmById(id));
    }

    /** Acknowledge an alarm. */
    @PostMapping("/{id}/acknowledge")
    public ResponseEntity<AlarmResponse> acknowledge(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Id", required = false) String acknowledgedBy) {
        return ResponseEntity.ok(alarmService.acknowledge(id, acknowledgedBy));
    }

    /** Clear / close an alarm. */
    @PostMapping("/{id}/clear")
    public ResponseEntity<AlarmResponse> clear(
            @PathVariable Long id,
            @RequestHeader(value = "X-User-Id", required = false) String clearedBy) {
        return ResponseEntity.ok(alarmService.clearAlarm(id, clearedBy));
    }

    /** View configured severity rules. */
    @GetMapping("/rules")
    public ResponseEntity<List<SeverityRuleResponse>> getRules() {
        return ResponseEntity.ok(severityRuleService.getRules());
    }

    /** Create a new severity rule, or update the existing one for that alarmType. */
    @PostMapping("/rules")
    public ResponseEntity<SeverityRuleResponse> upsertRule(@Valid @RequestBody SeverityRuleRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(severityRuleService.upsertRule(request));
    }

    /** Update an existing severity rule by id. */
    @PutMapping("/rules/{id}")
    public ResponseEntity<SeverityRuleResponse> updateRule(
            @PathVariable Long id, @Valid @RequestBody SeverityRuleRequest request) {
        return ResponseEntity.ok(severityRuleService.updateRule(id, request));
    }

    /** Delete a severity rule by id. */
    @DeleteMapping("/rules/{id}")
    public ResponseEntity<Void> deleteRule(@PathVariable Long id) {
        severityRuleService.deleteRule(id);
        return ResponseEntity.noContent().build();
    }
}
