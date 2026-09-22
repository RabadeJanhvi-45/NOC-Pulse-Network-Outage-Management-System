package com.noc.alarmservice.service;

import com.noc.alarmservice.dto.SeverityRuleRequest;
import com.noc.alarmservice.dto.SeverityRuleResponse;

import java.util.List;

public interface SeverityRuleService {

    List<SeverityRuleResponse> getRules();

    SeverityRuleResponse upsertRule(SeverityRuleRequest request);

    SeverityRuleResponse updateRule(Long id, SeverityRuleRequest request);

    void deleteRule(Long id);
}
