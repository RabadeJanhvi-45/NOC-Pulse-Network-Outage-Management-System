package com.noc.deviceservice.repository;

import com.noc.deviceservice.entity.DeviceHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DeviceHistoryRepository extends JpaRepository<DeviceHistory, Long> {

    List<DeviceHistory> findByDeviceIdOrderByChangedAtDesc(Long deviceId);
}
