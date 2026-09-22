package com.noc.authservice.service;

import com.noc.authservice.dto.RoleRequest;
import com.noc.authservice.dto.RoleResponse;
import com.noc.authservice.entity.Role;
import com.noc.authservice.exception.DuplicateResourceException;
import com.noc.authservice.exception.ResourceNotFoundException;
import com.noc.authservice.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;

    @Override
    @Transactional
    public RoleResponse createRole(RoleRequest request) {
        if (roleRepository.existsByNameIgnoreCase(request.getName())) {
            throw new DuplicateResourceException("A role named '" + request.getName() + "' already exists");
        }
        Role role = Role.builder()
                .name(request.getName())
                .description(request.getDescription())
                .build();
        Role saved = roleRepository.save(role);
        return toResponse(saved);
    }

    @Override
    public List<RoleResponse> getRoles() {
        return roleRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Override
    public RoleResponse getRoleById(Long id) {
        return toResponse(findRoleOrThrow(id));
    }

    @Override
    @Transactional
    public RoleResponse updateRole(Long id, RoleRequest request) {
        Role role = findRoleOrThrow(id);

        if (StringUtils.hasText(request.getName()) && !request.getName().equalsIgnoreCase(role.getName())
                && roleRepository.existsByNameIgnoreCase(request.getName())) {
            throw new DuplicateResourceException("A role named '" + request.getName() + "' already exists");
        }

        if (StringUtils.hasText(request.getName())) {
            role.setName(request.getName());
        }
        if (request.getDescription() != null) {
            role.setDescription(request.getDescription());
        }

        return toResponse(roleRepository.save(role));
    }

    @Override
    @Transactional
    public void deleteRole(Long id) {
        Role role = findRoleOrThrow(id);
        roleRepository.delete(role);
    }

    // ---- helpers ----

    private Role findRoleOrThrow(Long id) {
        return roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + id));
    }

    private RoleResponse toResponse(Role role) {
        return RoleResponse.builder()
                .id(role.getId())
                .name(role.getName())
                .description(role.getDescription())
                .build();
    }
}