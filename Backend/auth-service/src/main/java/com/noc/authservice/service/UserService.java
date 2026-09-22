package com.noc.authservice.service;

import com.noc.authservice.dto.UserRequest;
import com.noc.authservice.dto.UserResponse;

import java.util.List;

public interface UserService {

    UserResponse createUser(UserRequest request, String actor, String ipAddress);

    List<UserResponse> getUsers();

    UserResponse getUserById(Long id);

    UserResponse updateUser(Long id, UserRequest request, String actor, String ipAddress);

    void deleteUser(Long id, String actor, String ipAddress);
}
