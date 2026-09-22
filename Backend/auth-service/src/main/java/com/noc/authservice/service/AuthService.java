package com.noc.authservice.service;

import com.noc.authservice.dto.LoginRequest;
import com.noc.authservice.dto.LoginResponse;

public interface AuthService {

    LoginResponse login(LoginRequest request, String ipAddress);

    void logout(String username, String ipAddress);
}
