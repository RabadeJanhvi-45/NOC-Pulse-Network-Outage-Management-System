package com.noc.deviceservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Device inventory record.
 * Covers US-04 (Device Registration), US-05 (Edit Device Details),
 * US-06 (View Device Inventory).
 */
@Entity
@Table(name = "devices", uniqueConstraints = {
        @UniqueConstraint(name = "uk_device_business_id", columnNames = "device_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Device {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Unique business identifier (e.g. "RTR-001") — distinct from the DB primary key. */
    @Column(name = "device_id", nullable = false, unique = true, length = 100)
    private String deviceId;

    /** e.g. Router, Switch, Node */
    @Column(name = "device_type", nullable = false, length = 50)
    private String deviceType;

    @Column(nullable = false, length = 150)
    private String name;

    /** Management IP address — required at registration per US-04. */
    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(length = 150)
    private String location;

    @Column(length = 100)
    private String region;

    /** Active / Inactive / Faulty */
    @Column(nullable = false, length = 20)
    @Builder.Default
    private String status = "Active";

    /** Healthy / Degraded / Critical */
    @Column(nullable = false, length = 20)
    @Builder.Default
    private String health = "Healthy";

    @Column(name = "created_by", length = 100)
    private String createdBy;

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
