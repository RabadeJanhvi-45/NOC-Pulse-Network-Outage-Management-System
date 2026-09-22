package com.noc.incidentservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * A trackable network incident. Always created from an alarm now (alarmId
 * always set) via the internal from-alarm endpoint — manual creation was
 * removed per the rework spec.
 *
 * deviceId is device-service's business identifier — not an FK — validated
 * via a Feign call to device-service, per the cross-service communication rule.
 */
@Entity
@Table(name = "incidents")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Incident {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** device-service's business deviceId, validated via Feign before save. */
    @Column(name = "device_id", nullable = false, length = 100)
    private String deviceId;

    /** alarm-service's alarm id this incident originated from. Null if manually created. */
    @Column(name = "alarm_id")
    private Long alarmId;

    @Column(length = 1000)
    private String description;

    /** High / Medium / Low */
    @Column(nullable = false, length = 20)
    private String priority;

        /** NEW / ASSIGNED / IN_PROGRESS / RESOLVED / CLOSED */
    @Column(nullable = false, length = 20)
    @Builder.Default
    private String status = "NEW";

    @Column(name = "resolution_notes", length = 2000)
    private String resolutionNotes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

        @Column(name = "closed_at")
    private LocalDateTime closedAt;

    @Column(name = "closed_by", length = 100)
    private String closedBy;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
