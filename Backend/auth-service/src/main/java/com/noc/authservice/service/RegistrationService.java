package com.noc.authservice.service;

import com.noc.authservice.dto.EngineerRegisterRequest;
import com.noc.authservice.dto.RegisterRequest;
import com.noc.authservice.dto.RegistrationRequestResponse;

import java.util.List;

public interface RegistrationService {

    RegistrationRequestResponse register(RegisterRequest request);

    RegistrationRequestResponse registerEngineer(EngineerRegisterRequest request);

    List<RegistrationRequestResponse> getRequests();

    RegistrationRequestResponse approve(Long id, Long reviewerId);

    RegistrationRequestResponse reject(Long id, Long reviewerId, String reason);
}