package com.noc.deviceservice.service;

import com.noc.deviceservice.dto.DeviceHistoryResponse;
import com.noc.deviceservice.dto.DeviceRequest;
import com.noc.deviceservice.dto.DeviceResponse;

import java.util.List;

public interface DeviceService {

    DeviceResponse registerDevice(DeviceRequest request, String changedBy);

    List<DeviceResponse> getDevices(String region, String status, String deviceType, String search, String callerRole, String callerUsername, String callerUserId);

    DeviceResponse getDeviceById(Long id);

    DeviceResponse getDeviceByBusinessId(String deviceId);

    DeviceResponse updateDevice(Long id, DeviceRequest request, String changedBy);

    void deleteDevice(Long id);

        List<DeviceHistoryResponse> getHistory(Long id);

    /** Applies an Admin-approved critical-field change directly, bypassing the request-routing in updateDevice. */
    DeviceResponse applyApprovedFieldChange(Long deviceId, String deviceType, String ipAddress, String changedBy);

    /** Applies an Admin-approved deactivation directly, bypassing the request-routing in updateDevice. */
       DeviceResponse applyApprovedDeactivation(Long deviceId, String changedBy);

    /** Used by the internal active-check endpoint — alarm-service calls this via Feign before raising an alarm. */
    boolean isDeviceActive(String deviceId);
}
