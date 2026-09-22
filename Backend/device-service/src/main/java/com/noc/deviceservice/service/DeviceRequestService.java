package com.noc.deviceservice.service;

import com.noc.deviceservice.dto.DeviceRegistrationRequestResponse;
import com.noc.deviceservice.dto.DeviceRequestSubmission;
import com.noc.deviceservice.dto.DeviceResponse;

import java.util.List;

public interface DeviceRequestService {

    DeviceRegistrationRequestResponse submitNewDeviceRequest(DeviceRequestSubmission submission, String requestedBy);

    List<DeviceRegistrationRequestResponse> getRequests();

    /** Branches on the request's requestType: NEW creates a Device, FIELD_CHANGE/DEACTIVATION apply to the existing target device. */
    DeviceResponse approve(Long id, String reviewedBy);

    DeviceRegistrationRequestResponse rejectRequest(Long id, String reviewedBy, String reason);
}