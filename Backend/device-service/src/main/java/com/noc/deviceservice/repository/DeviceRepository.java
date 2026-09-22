package com.noc.deviceservice.repository;

import com.noc.deviceservice.entity.Device;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface DeviceRepository extends JpaRepository<Device, Long>, JpaSpecificationExecutor<Device> {

    boolean existsByDeviceIdIgnoreCase(String deviceId);

    Optional<Device> findByDeviceIdIgnoreCase(String deviceId);
}
