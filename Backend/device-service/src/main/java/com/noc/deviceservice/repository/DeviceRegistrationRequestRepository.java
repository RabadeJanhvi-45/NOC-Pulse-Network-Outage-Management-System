package com.noc.deviceservice.repository;

import com.noc.deviceservice.entity.DeviceRegistrationRequest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeviceRegistrationRequestRepository extends JpaRepository<DeviceRegistrationRequest, Long> {
}