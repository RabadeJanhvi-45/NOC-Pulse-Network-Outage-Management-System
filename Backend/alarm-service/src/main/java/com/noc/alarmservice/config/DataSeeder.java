package com.noc.alarmservice.config;

import com.noc.alarmservice.entity.SeverityRule;
import com.noc.alarmservice.repository.SeverityRuleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final SeverityRuleRepository severityRuleRepository;

    @Override
    public void run(String... args) {
        if (severityRuleRepository.count() == 0) {
            severityRuleRepository.saveAll(List.of(
                    SeverityRule.builder().alarmType("LinkDown").severity("Critical").conditionLogic("Interface carrier lost").build(),
                    SeverityRule.builder().alarmType("PowerFailure").severity("Critical").conditionLogic("PSU offline").build(),
                    SeverityRule.builder().alarmType("HighCPU").severity("Major").conditionLogic("CPU load > 90% for 5m").build(),
                    SeverityRule.builder().alarmType("HighMemory").severity("Major").conditionLogic("RAM utilization > 85%").build(),
                    SeverityRule.builder().alarmType("LinkFlap").severity("Minor").conditionLogic("State change > 3 times in 1m").build(),
                    SeverityRule.builder().alarmType("FanFailure").severity("Warning").conditionLogic("Fan RPM below threshold").build()
            ));
            log.info("alarm-service: Seeded default Severity Rules.");
        }
    }
}
