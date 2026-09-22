package com.noc.authservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Security-relevant audit trail: successful/failed logins, logouts,
 * unauthorized access attempts, and administrative changes to users,
 * roles and permissions.
 */
@Entity
@Table(name = "audit_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Nullable — e.g. a failed login for a username that doesn't exist. */
    @Column(name = "user_id")
    private Long userId;

    /** Denormalized so the log stays readable even if the user is later deleted. */
    @Column(length = 100)
    private String username;

    /** LOGIN_SUCCESS / LOGIN_FAILURE / LOGOUT / UNAUTHORIZED_ACCESS /
     *  USER_CREATED / USER_UPDATED / USER_DELETED / ROLE_CREATED / ... */
    @Column(nullable = false, length = 50)
    private String action;

    @Column(length = 500)
    private String details;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
