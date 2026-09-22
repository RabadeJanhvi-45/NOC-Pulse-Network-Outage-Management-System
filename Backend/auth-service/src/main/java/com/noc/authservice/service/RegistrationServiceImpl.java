package com.noc.authservice.service;

import com.noc.authservice.dto.RegisterRequest;
import com.noc.authservice.dto.RegistrationRequestResponse;
import com.noc.authservice.entity.RegistrationRequest;
import com.noc.authservice.entity.Role;
import com.noc.authservice.entity.User;
import com.noc.authservice.exception.DuplicateResourceException;
import com.noc.authservice.exception.ResourceNotFoundException;
import com.noc.authservice.repository.RegistrationRequestRepository;
import com.noc.authservice.repository.RoleRepository;
import com.noc.authservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * NOC_OPERATOR self-registration: register() creates a disabled User with
 * a PENDING RegistrationRequest; the account only becomes usable once an
 * Admin calls approve(). Notification-to-Admins hookup lands in Part D
 * once the Notification entity/service exists — TODO wire it in here.
 */
@Service
@RequiredArgsConstructor
public class RegistrationServiceImpl implements RegistrationService {

    private static final String PENDING = "PENDING";
    private static final String APPROVED = "APPROVED";
    private static final String REJECTED = "REJECTED";
    private static final String NOC_OPERATOR_ROLE = "NOC_OPERATOR";
    private static final String ENGINEER_ROLE = "ENGINEER";

    private final RegistrationRequestRepository registrationRequestRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final com.noc.authservice.repository.EngineerProfileRepository engineerProfileRepository;
    private final PasswordEncoder passwordEncoder;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public RegistrationRequestResponse register(RegisterRequest request) {
        if (userRepository.existsByUsernameIgnoreCase(request.getUsername())) {
            throw new DuplicateResourceException("A user with username '" + request.getUsername() + "' already exists");
        }

        boolean isEngineer = "ENGINEER".equalsIgnoreCase(request.getRole())
                || (request.getSpecialization() != null && !request.getSpecialization().isBlank());

        String targetRoleName = isEngineer ? ENGINEER_ROLE : NOC_OPERATOR_ROLE;

        Role role = roleRepository.findByNameIgnoreCase(targetRoleName)
                .orElseThrow(() -> new IllegalStateException(targetRoleName + " role is not seeded"));

        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .email(request.getEmail())
                .fullName(request.getFullName())
                .roleId(role.getId())
                .enabled(false)
                .build();
        User savedUser = userRepository.save(user);

        if (isEngineer && request.getSpecialization() != null && !request.getSpecialization().isBlank()) {
            engineerProfileRepository.save(com.noc.authservice.entity.EngineerProfile.builder()
                    .userId(savedUser.getId())
                    .primarySpecialization(request.getSpecialization())
                    .activeTaskLimit(5)
                    .build());
        }

        RegistrationRequest registrationRequest = RegistrationRequest.builder()
                .userId(savedUser.getId())
                .status(PENDING)
                .reviewedAt(null)
                .build();
        RegistrationRequest saved = registrationRequestRepository.save(registrationRequest);

        notificationService.notifyRole("ADMIN", "REGISTRATION_REQUEST",
                "New " + targetRoleName + " registration request from '" + savedUser.getUsername() + "'");
        return toResponse(saved, savedUser);
    }

    @Override
    @Transactional
    public RegistrationRequestResponse registerEngineer(com.noc.authservice.dto.EngineerRegisterRequest request) {
        if (userRepository.existsByUsernameIgnoreCase(request.getUsername())) {
            throw new DuplicateResourceException("A user with username '" + request.getUsername() + "' already exists");
        }

        Role role = roleRepository.findByNameIgnoreCase(ENGINEER_ROLE)
                .orElseThrow(() -> new IllegalStateException("ENGINEER role is not seeded"));

        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .email(request.getEmail())
                .fullName(request.getFullName())
                .roleId(role.getId())
                .enabled(false)
                .build();
        User savedUser = userRepository.save(user);

        if (request.getSpecialization() != null && !request.getSpecialization().isBlank()) {
            engineerProfileRepository.save(com.noc.authservice.entity.EngineerProfile.builder()
                    .userId(savedUser.getId())
                    .primarySpecialization(request.getSpecialization())
                    .activeTaskLimit(5)
                    .build());
        }

        RegistrationRequest registrationRequest = RegistrationRequest.builder()
                .userId(savedUser.getId())
                .status(PENDING)
                .reviewedAt(null)
                .build();
        RegistrationRequest saved = registrationRequestRepository.save(registrationRequest);

        notificationService.notifyRole("ADMIN", "REGISTRATION_REQUEST",
                "New ENGINEER registration request from '" + savedUser.getUsername() + "'");
        return toResponse(saved, savedUser);
    }

    @Override
    public List<RegistrationRequestResponse> getRequests() {
        return registrationRequestRepository.findAll().stream()
                .map(rr -> toResponse(rr, findUserOrThrow(rr.getUserId())))
                .toList();
    }

    @Override
    @Transactional
    public RegistrationRequestResponse approve(Long id, Long reviewerId) {
        RegistrationRequest registrationRequest = findRequestOrThrow(id);
        requirePending(registrationRequest);
        User user = findUserOrThrow(registrationRequest.getUserId());

        user.setEnabled(true);
        userRepository.save(user);

        registrationRequest.setStatus(APPROVED);
        registrationRequest.setReviewedBy(reviewerId);
        registrationRequest.setReviewedAt(LocalDateTime.now());
        RegistrationRequest saved = registrationRequestRepository.save(registrationRequest);

                notificationService.notifyUser(user.getId(), "REGISTRATION_DECISION", "Your registration was approved.");

        return toResponse(saved, user);
    }

    @Override
    @Transactional
    public RegistrationRequestResponse reject(Long id, Long reviewerId, String reason) {
        RegistrationRequest registrationRequest = findRequestOrThrow(id);
        requirePending(registrationRequest);
        User user = findUserOrThrow(registrationRequest.getUserId());

        registrationRequest.setStatus(REJECTED);
        registrationRequest.setRejectionReason(reason);
        registrationRequest.setReviewedBy(reviewerId);
        registrationRequest.setReviewedAt(LocalDateTime.now());
        RegistrationRequest saved = registrationRequestRepository.save(registrationRequest);

              notificationService.notifyUser(user.getId(), "REGISTRATION_DECISION", "Your registration was rejected: " + reason);

        return toResponse(saved, user);
    }

    // ---- helpers ----

    private RegistrationRequest findRequestOrThrow(Long id) {
        return registrationRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Registration request not found with id: " + id));
    }

    private User findUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
    }

    private void requirePending(RegistrationRequest registrationRequest) {
        if (!PENDING.equalsIgnoreCase(registrationRequest.getStatus())) {
            throw new IllegalStateException("Registration request has already been reviewed");
        }
    }

    private RegistrationRequestResponse toResponse(RegistrationRequest rr, User user) {
        String roleName = roleRepository.findById(user.getRoleId())
                .map(Role::getName)
                .orElse("UNKNOWN");

        return RegistrationRequestResponse.builder()
                .id(rr.getId())
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .roleName(roleName)
                .status(rr.getStatus())
                .rejectionReason(rr.getRejectionReason())
                .reviewedBy(rr.getReviewedBy())
                .requestedAt(rr.getRequestedAt())
                .reviewedAt(rr.getReviewedAt())
                .build();
    }
}