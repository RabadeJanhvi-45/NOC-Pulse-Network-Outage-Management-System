package com.noc.alarmservice.repository;

import com.noc.alarmservice.entity.SeverityRule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SeverityRuleRepository extends JpaRepository<SeverityRule, Long> {

    Optional<SeverityRule> findByAlarmTypeIgnoreCase(String alarmType);
}
