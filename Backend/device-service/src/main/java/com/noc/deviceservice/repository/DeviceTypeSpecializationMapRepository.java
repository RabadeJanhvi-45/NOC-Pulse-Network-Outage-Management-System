package com.noc.deviceservice.repository;

import com.noc.deviceservice.entity.DeviceTypeSpecializationMap;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DeviceTypeSpecializationMapRepository extends JpaRepository<DeviceTypeSpecializationMap, Long> {

    Optional<DeviceTypeSpecializationMap> findByDeviceTypeIgnoreCase(String deviceType);

    boolean existsByDeviceTypeIgnoreCase(String deviceType);
}