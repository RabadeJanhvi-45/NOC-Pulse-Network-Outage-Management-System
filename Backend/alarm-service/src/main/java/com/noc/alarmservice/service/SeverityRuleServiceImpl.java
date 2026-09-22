package com.noc.alarmservice.service;

import com.noc.alarmservice.dto.SeverityRuleRequest;
import com.noc.alarmservice.dto.SeverityRuleResponse;
import com.noc.alarmservice.entity.SeverityRule;
import com.noc.alarmservice.repository.SeverityRuleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class SeverityRuleServiceImpl implements SeverityRuleService {

    private static final Set<String> VALID_SEVERITIES = Set.of("Critical", "Major", "Minor", "Warning");

    private final SeverityRuleRepository severityRuleRepository;

    @Override
    public List<SeverityRuleResponse> getRules() {
        return severityRuleRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public SeverityRuleResponse upsertRule(SeverityRuleRequest request) {
        boolean valid = VALID_SEVERITIES.stream().anyMatch(s -> s.equalsIgnoreCase(request.getSeverity()));
        if (!valid) {
            throw new IllegalArgumentException(
                    "severity must be one of " + VALID_SEVERITIES + " but was '" + request.getSeverity() + "'");
        }

        SeverityRule rule = severityRuleRepository.findByAlarmTypeIgnoreCase(request.getAlarmType())
                .orElseGet(() -> SeverityRule.builder().alarmType(request.getAlarmType()).build());

        rule.setSeverity(request.getSeverity());
        rule.setConditionLogic(request.getConditionLogic());

        SeverityRule saved = severityRuleRepository.save(rule);
        return toResponse(saved);
    }

    @Override
    @Transactional
    public SeverityRuleResponse updateRule(Long id, SeverityRuleRequest request) {
        boolean valid = VALID_SEVERITIES.stream().anyMatch(s -> s.equalsIgnoreCase(request.getSeverity()));
        if (!valid) {
            throw new IllegalArgumentException(
                    "severity must be one of " + VALID_SEVERITIES + " but was '" + request.getSeverity() + "'");
        }

        SeverityRule rule = severityRuleRepository.findById(id)
                .orElseThrow(() -> new com.noc.alarmservice.exception.ResourceNotFoundException("Severity rule not found with id: " + id));

        rule.setAlarmType(request.getAlarmType());
        rule.setSeverity(request.getSeverity());
        rule.setConditionLogic(request.getConditionLogic());

        SeverityRule saved = severityRuleRepository.save(rule);
        return toResponse(saved);
    }

    @Override
    @Transactional
    public void deleteRule(Long id) {
        SeverityRule rule = severityRuleRepository.findById(id)
                .orElseThrow(() -> new com.noc.alarmservice.exception.ResourceNotFoundException("Severity rule not found with id: " + id));
        severityRuleRepository.delete(rule);
    }

    private SeverityRuleResponse toResponse(SeverityRule r) {
        return SeverityRuleResponse.builder()
                .id(r.getId())
                .alarmType(r.getAlarmType())
                .severity(r.getSeverity())
                .conditionLogic(r.getConditionLogic())
                .build();
    }
}
