package com.noc.authservice.service;

import com.noc.authservice.dto.AuditLogResponse;
import com.noc.authservice.entity.AuditLog;
import com.noc.authservice.repository.AuditLogRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuditLogServiceImpl implements AuditLogService {

    private final AuditLogRepository auditLogRepository;

    @Override
    @Transactional
    public void log(Long userId, String username, String action, String details, String ipAddress) {
        AuditLog entry = AuditLog.builder()
                .userId(userId)
                .username(username)
                .action(action)
                .details(details)
                .ipAddress(ipAddress)
                .build();
        auditLogRepository.save(entry);
    }

    @Override
    public List<AuditLogResponse> getLogs(String action, String username) {
        Specification<AuditLog> spec = buildFilterSpec(action, username);
        return auditLogRepository.findAll(spec).stream()
                .sorted(Comparator.comparing(AuditLog::getCreatedAt).reversed())
                .map(this::toResponse)
                .toList();
    }

    private Specification<AuditLog> buildFilterSpec(String action, String username) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (StringUtils.hasText(action)) {
                predicates.add(cb.equal(cb.lower(root.get("action")), action.toLowerCase()));
            }
            if (StringUtils.hasText(username)) {
                predicates.add(cb.equal(cb.lower(root.get("username")), username.toLowerCase()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private AuditLogResponse toResponse(AuditLog a) {
        return AuditLogResponse.builder()
                .id(a.getId())
                .userId(a.getUserId())
                .username(a.getUsername())
                .action(a.getAction())
                .details(a.getDetails())
                .ipAddress(a.getIpAddress())
                .createdAt(a.getCreatedAt())
                .build();
    }
}
