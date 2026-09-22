package com.noc.authservice.service;

import com.noc.authservice.dto.AuditLogResponse;

import java.util.List;

public interface AuditLogService {

    void log(Long userId, String username, String action, String details, String ipAddress);

    List<AuditLogResponse> getLogs(String action, String username);
}
