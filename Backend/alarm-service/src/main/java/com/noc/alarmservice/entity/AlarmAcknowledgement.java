package com.noc.alarmservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/** One row per acknowledgement action taken on an Alarm. */
@Entity
@Table(name = "alarm_acknowledgements")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlarmAcknowledgement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "alarm_id", nullable = false)
    private Long alarmId;

    @Column(name = "acknowledged_by", nullable = false, length = 100)
    private String acknowledgedBy;

    @Column(name = "acknowledged_at", nullable = false, updatable = false)
    private LocalDateTime acknowledgedAt;

    @PrePersist
    protected void onCreate() {
        this.acknowledgedAt = LocalDateTime.now();
    }
}
