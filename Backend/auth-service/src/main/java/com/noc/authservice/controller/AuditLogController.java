package com.noc.authservice.controller;

import com.noc.authservice.dto.AuditLogResponse;
import com.noc.authservice.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Read-only view of the audit trail — reachable through api-gateway at
 * /api/audit-logs. Covers the "unauthorized access logging" user story
 * (surfaces LOGIN_FAILURE / LOGOUT / USER_* / ROLE_* entries written by
 * AuthService, UserService and RoleService).
 */
@RestController
@RequestMapping("/api/audit-logs")
@RequiredArgsConstructor
public class AuditLogController {

    private final AuditLogService auditLogService;

    @GetMapping
    public ResponseEntity<List<AuditLogResponse>> getLogs(
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String username) {
        return ResponseEntity.ok(auditLogService.getLogs(action, username));
    }
}
