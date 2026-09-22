package com.noc.deviceservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Admin-approval workflow for devices. requestType distinguishes what
 * kind of change this row represents:
 *  - NEW: register a brand-new device (Part A)
 *  - FIELD_CHANGE: edit a critical field on an existing device (Part B)
 *  - DEACTIVATION: set an existing device's status to Inactive (Part B)
 * For FIELD_CHANGE/DEACTIVATION, targetDeviceId points at the existing
 * Device.id being changed; for NEW it's null.
 */
@Entity
@Table(name = "device_registration_requests")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeviceRegistrationRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** NEW / FIELD_CHANGE / DEACTIVATION */
    @Column(name = "request_type", nullable = false, length = 20)
    @Builder.Default
    private String requestType = "NEW";

    /** Set for FIELD_CHANGE/DEACTIVATION — the existing Device.id being changed. Null for NEW. */
    @Column(name = "target_device_id")
    private Long targetDeviceId;

    @Column(name = "device_id", length = 100)
    private String deviceId;

    @Column(name = "device_type", length = 50)
    private String deviceType;

    @Column(length = 150)
    private String name;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(length = 150)
    private String location;

    @Column(length = 100)
    private String region;

    @Column(name = "requested_by", length = 100)
    private String requestedBy;

    /** PENDING / APPROVED / REJECTED */
    @Column(nullable = false, length = 20)
    @Builder.Default
    private String status = "PENDING";

    @Column(name = "rejection_reason", length = 500)
    private String rejectionReason;

    @Column(name = "reviewed_by", length = 100)
    private String reviewedBy;

    @Column(name = "requested_at", nullable = false, updatable = false)
    private LocalDateTime requestedAt;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @PrePersist
    protected void onCreate() {
        this.requestedAt = LocalDateTime.now();
    }
}