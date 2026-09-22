package com.noc.authservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * One row per Engineer. primarySpecialization is Admin-set only (see
 * EngineerController); activeTaskLimit feeds incident-service's
 * assignment engine via the internal GET /api/engineers endpoint.
 */
@Entity
@Table(name = "engineer_profiles", uniqueConstraints = {
        @UniqueConstraint(name = "uk_engineer_profile_user", columnNames = "user_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EngineerProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "primary_specialization", length = 100)
    private String primarySpecialization;

    @Column(name = "active_task_limit", nullable = false)
    @Builder.Default
    private Integer activeTaskLimit = 5;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}