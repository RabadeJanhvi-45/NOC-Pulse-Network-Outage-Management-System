package com.noc.incidentservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/** An escalation raised against an incident, either manually or by the SLA-breach scan. */
@Entity
@Table(name = "escalations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Escalation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "incident_id", nullable = false)
    private Long incidentId;

    /** Null when system-triggered (e.g. sla_breach). */
    @Column(name = "escalated_by", length = 100)
    private String escalatedBy;

    @Column(length = 500)
    private String reason;

    /** manual / sla_breach */
    @Column(name = "trigger_type", nullable = false, length = 20)
    private String triggerType;

    @Column(name = "escalated_at", nullable = false, updatable = false)
    private LocalDateTime escalatedAt;

    @PrePersist
    protected void onCreate() {
        this.escalatedAt = LocalDateTime.now();
    }
}
