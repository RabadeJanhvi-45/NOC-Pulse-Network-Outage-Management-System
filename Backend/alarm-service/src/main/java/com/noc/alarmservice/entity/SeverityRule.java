package com.noc.alarmservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Maps an alarmType to the severity that should be auto-assigned when no
 * explicit severity is supplied on alarm creation.
 */
@Entity
@Table(name = "severity_rules", uniqueConstraints = {
        @UniqueConstraint(name = "uk_severity_rule_alarm_type", columnNames = "alarm_type")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SeverityRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "alarm_type", nullable = false, unique = true, length = 100)
    private String alarmType;

    /** Resulting severity applied when alarmType matches this rule. */
    @Column(nullable = false, length = 20)
    private String severity;

    /** Free-text description of the matching logic (e.g. "latency > 500ms for 5min"). */
    @Column(name = "condition_logic", length = 255)
    private String conditionLogic;
}
