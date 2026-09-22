package com.noc.deviceservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Maps a device type (e.g. "Router") to the Engineer specialization
 * required to work incidents on it (e.g. "Networking"). Consumed by
 * incident-service's assignment engine via the internal lookup endpoint.
 */
@Entity
@Table(name = "device_type_specialization_map", uniqueConstraints = {
        @UniqueConstraint(name = "uk_device_type", columnNames = "device_type")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeviceTypeSpecializationMap {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "device_type", nullable = false, unique = true, length = 50)
    private String deviceType;

    @Column(name = "required_specialization", nullable = false, length = 100)
    private String requiredSpecialization;
}