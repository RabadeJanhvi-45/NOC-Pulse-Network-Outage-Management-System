package com.noc.alarmservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Raw alarm raised against a device.
 * deviceId is a reference to device-service's business deviceId (validated
 * via Feign on creation) — not a DB foreign key, per the cross-service rule
 * in the Backend Guide (services never share databases).
 */
@Entity
@Table(name = "alarms")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Alarm {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "device_id", nullable = false, length = 100)
    private String deviceId;

    /** e.g. Link Down, High Latency */
    @Column(name = "alarm_type", nullable = false, length = 100)
    private String alarmType;

    /** Critical / Major / Minor / Warning */
    @Column(nullable = false, length = 20)
    private String severity;

    /** Active / Acknowledged / Cleared */
    @Column(nullable = false, length = 20)
    @Builder.Default
    private String status = "Active";

    /** Used to cluster duplicate alarms from the same fault (deviceId + alarmType). */
    @Column(name = "group_key", nullable = false, length = 200)
    private String groupKey;

    @Column(name = "raised_by", length = 100)
    private String raisedBy;

    @Column(name = "raised_at", nullable = false, updatable = false)
    private LocalDateTime raisedAt;

    @PrePersist
    protected void onCreate() {
        this.raisedAt = LocalDateTime.now();
    }
}
