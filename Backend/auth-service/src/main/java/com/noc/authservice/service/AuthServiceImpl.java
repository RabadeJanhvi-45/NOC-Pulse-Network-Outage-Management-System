package com.noc.authservice.service;

import com.noc.authservice.config.JwtUtil;
import com.noc.authservice.dto.LoginRequest;
import com.noc.authservice.dto.LoginResponse;
import com.noc.authservice.entity.Role;
import com.noc.authservice.entity.User;
import com.noc.authservice.exception.InvalidCredentialsException;
import com.noc.authservice.repository.RoleRepository;
import com.noc.authservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * Handles login/logout: credential verification, JWT issuing, and writing
 * every attempt (success, failure, and logout) to the audit trail — covers
 * the "unauthorized access logging" user story alongside login/logout.
 */
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private static final String GENERIC_LOGIN_FAILURE_MESSAGE = "Invalid username or password";

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuditLogService auditLogService;

    @Override
    public LoginResponse login(LoginRequest request, String ipAddress) {
        User user = userRepository.findByUsernameIgnoreCase(request.getUsername()).orElse(null);

        if (user == null) {
            auditLogService.log(null, request.getUsername(), "LOGIN_FAILURE",
                    "No account with this username", ipAddress);
            throw new InvalidCredentialsException(GENERIC_LOGIN_FAILURE_MESSAGE);
        }

        if (!Boolean.TRUE.equals(user.getEnabled())) {
            auditLogService.log(user.getId(), user.getUsername(), "LOGIN_FAILURE",
                    "Account is disabled or pending approval", ipAddress);
            throw new InvalidCredentialsException("Your account is pending administrator approval or has been disabled.");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            auditLogService.log(user.getId(), user.getUsername(), "LOGIN_FAILURE",
                    "Incorrect password", ipAddress);
            throw new InvalidCredentialsException(GENERIC_LOGIN_FAILURE_MESSAGE);
        }

        Role role = roleRepository.findById(user.getRoleId()).orElse(null);
        String roleName = role != null ? role.getName() : null;

        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), roleName);
        auditLogService.log(user.getId(), user.getUsername(), "LOGIN_SUCCESS", null, ipAddress);

        LocalDateTime expiresAt = LocalDateTime.now().plus(java.time.Duration.ofMillis(jwtUtil.getExpirationMs()));

        return LoginResponse.builder()
                .token(token)
                .userId(user.getId())
                .username(user.getUsername())
                .roleName(roleName)
                .expiresAt(expiresAt)
                .build();
    }

    @Override
    public void logout(String username, String ipAddress) {
        User user = userRepository.findByUsernameIgnoreCase(username).orElse(null);
        auditLogService.log(user != null ? user.getId() : null, username, "LOGOUT", null, ipAddress);
    }
}
