package com.noc.incidentservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "engineer_rejections")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EngineerRejection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "incident_id", nullable = false)
    private Long incidentId;

    @Column(name = "engineer_id", nullable = false, length = 100)
    private String engineerId;

    @Column(nullable = false, length = 2000)
    private String reason;

    @Column(name = "admin_decision", length = 20)
    private String adminDecision;

    @Column(name = "decided_by", length = 100)
    private String decidedBy;

    @Column(name = "rejected_at", nullable = false, updatable = false)
    private LocalDateTime rejectedAt;

    @Column(name = "decided_at")
    private LocalDateTime decidedAt;

    @PrePersist
    protected void onCreate() {
        this.rejectedAt = LocalDateTime.now();
    }
}