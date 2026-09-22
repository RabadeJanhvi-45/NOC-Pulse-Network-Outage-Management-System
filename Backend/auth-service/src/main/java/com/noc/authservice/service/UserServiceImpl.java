package com.noc.authservice.service;

import com.noc.authservice.dto.UserRequest;
import com.noc.authservice.dto.UserResponse;
import com.noc.authservice.entity.Role;
import com.noc.authservice.entity.User;
import com.noc.authservice.exception.DuplicateResourceException;
import com.noc.authservice.exception.ResourceNotFoundException;
import com.noc.authservice.repository.RoleRepository;
import com.noc.authservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditLogService auditLogService;

    @Override
    @Transactional
    public UserResponse createUser(UserRequest request, String actor, String ipAddress) {
        if (userRepository.existsByUsernameIgnoreCase(request.getUsername())) {
            throw new DuplicateResourceException("A user with username '" + request.getUsername() + "' already exists");
        }
        if (!StringUtils.hasText(request.getPassword())) {
            throw new IllegalArgumentException("password is required when creating a user");
        }
        Role role = findRoleOrThrow(request.getRoleId());

        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .email(request.getEmail())
                .fullName(request.getFullName())
                .roleId(role.getId())
                .enabled(request.getEnabled() != null ? request.getEnabled() : true)
                .build();

        User saved = userRepository.save(user);
        auditLogService.log(saved.getId(), saved.getUsername(), "USER_CREATED",
                "created by " + defaultActor(actor), ipAddress);
        return toResponse(saved, role.getName());
    }

    @Override
    public List<UserResponse> getUsers() {
        return userRepository.findAll().stream()
                .map(u -> toResponse(u, roleNameOf(u.getRoleId())))
                .toList();
    }

    @Override
    public UserResponse getUserById(Long id) {
        User user = findUserOrThrow(id);
        return toResponse(user, roleNameOf(user.getRoleId()));
    }

    @Override
    @Transactional
    public UserResponse updateUser(Long id, UserRequest request, String actor, String ipAddress) {
        User user = findUserOrThrow(id);

        if (StringUtils.hasText(request.getUsername()) && !request.getUsername().equalsIgnoreCase(user.getUsername())
                && userRepository.existsByUsernameIgnoreCase(request.getUsername())) {
            throw new DuplicateResourceException("A user with username '" + request.getUsername() + "' already exists");
        }

        if (StringUtils.hasText(request.getUsername())) {
            user.setUsername(request.getUsername());
        }
        if (StringUtils.hasText(request.getPassword())) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        if (request.getEmail() != null) {
            user.setEmail(request.getEmail());
        }
        if (request.getFullName() != null) {
            user.setFullName(request.getFullName());
        }
        if (request.getRoleId() != null) {
            findRoleOrThrow(request.getRoleId());
            user.setRoleId(request.getRoleId());
        }
        if (request.getEnabled() != null) {
            user.setEnabled(request.getEnabled());
        }

        User saved = userRepository.save(user);
        auditLogService.log(saved.getId(), saved.getUsername(), "USER_UPDATED",
                "updated by " + defaultActor(actor), ipAddress);
        return toResponse(saved, roleNameOf(saved.getRoleId()));
    }

    @Override
    @Transactional
    public void deleteUser(Long id, String actor, String ipAddress) {
        User user = findUserOrThrow(id);
        userRepository.delete(user);
        auditLogService.log(id, user.getUsername(), "USER_DELETED",
                "deleted by " + defaultActor(actor), ipAddress);
    }

    // ---- helpers ----

    private User findUserOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }

    private Role findRoleOrThrow(Long roleId) {
        return roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + roleId));
    }

    private String roleNameOf(Long roleId) {
        return roleRepository.findById(roleId).map(Role::getName).orElse(null);
    }

    private String defaultActor(String actor) {
        return StringUtils.hasText(actor) ? actor : "system";
    }

    private UserResponse toResponse(User u, String roleName) {
        return UserResponse.builder()
                .id(u.getId())
                .username(u.getUsername())
                .email(u.getEmail())
                .fullName(u.getFullName())
                .roleId(u.getRoleId())
                .roleName(roleName)
                .enabled(u.getEnabled())
                .createdAt(u.getCreatedAt())
                .updatedAt(u.getUpdatedAt())
                .build();
    }
}
