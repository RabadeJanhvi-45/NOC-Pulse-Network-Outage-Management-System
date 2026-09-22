package com.noc.authservice.service;

import com.noc.authservice.dto.EngineerResponse;
import com.noc.authservice.dto.EngineerSkillResponse;
import com.noc.authservice.entity.EngineerProfile;
import com.noc.authservice.entity.EngineerSkill;
import com.noc.authservice.entity.Role;
import com.noc.authservice.entity.User;
import com.noc.authservice.exception.DuplicateResourceException;
import com.noc.authservice.exception.ResourceNotFoundException;
import com.noc.authservice.repository.EngineerProfileRepository;
import com.noc.authservice.repository.EngineerSkillRepository;
import com.noc.authservice.repository.RoleRepository;
import com.noc.authservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EngineerServiceImpl implements EngineerService {

    private static final String ENGINEER_ROLE = "ENGINEER";
    private static final int MAX_SKILLS = 3;

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final EngineerProfileRepository engineerProfileRepository;
    private final EngineerSkillRepository engineerSkillRepository;

    @Override
    public List<EngineerResponse> getEngineers() {
        Role engineerRole = roleRepository.findByNameIgnoreCase(ENGINEER_ROLE)
                .orElseThrow(() -> new IllegalStateException("ENGINEER role is not seeded"));

        return userRepository.findAll().stream()
                .filter(u -> engineerRole.getId().equals(u.getRoleId()))
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public EngineerResponse setSpecialization(Long userId, String specialization) {
        User user = findEngineerOrThrow(userId);
        EngineerProfile profile = engineerProfileRepository.findByUserId(userId)
                .orElseGet(() -> EngineerProfile.builder().userId(userId).build());
        profile.setPrimarySpecialization(specialization);
        engineerProfileRepository.save(profile);
        return toResponse(user);
    }

    @Override
    public List<EngineerSkillResponse> getMySkills(Long userId) {
        return engineerSkillRepository.findByUserId(userId).stream()
                .map(this::toSkillResponse)
                .toList();
    }

    @Override
    @Transactional
    public EngineerSkillResponse addMySkill(Long userId, String skillName) {
        if (engineerSkillRepository.countByUserId(userId) >= MAX_SKILLS) {
            throw new IllegalArgumentException("An Engineer can have at most " + MAX_SKILLS + " secondary skills");
        }
        boolean alreadyHasSkill = engineerSkillRepository.findByUserId(userId).stream()
                .anyMatch(s -> s.getSkillName().equalsIgnoreCase(skillName));
        if (alreadyHasSkill) {
            throw new DuplicateResourceException("Skill '" + skillName + "' is already added");
        }
        EngineerSkill saved = engineerSkillRepository.save(
                EngineerSkill.builder().userId(userId).skillName(skillName).build());
        return toSkillResponse(saved);
    }

    @Override
    @Transactional
    public void deleteMySkill(Long userId, Long skillId) {
        engineerSkillRepository.deleteByIdAndUserId(skillId, userId);
    }

    // ---- helpers ----

    private User findEngineerOrThrow(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        Role role = roleRepository.findById(user.getRoleId())
                .orElseThrow(() -> new ResourceNotFoundException("Role not found for user: " + userId));
        if (!ENGINEER_ROLE.equalsIgnoreCase(role.getName())) {
            throw new IllegalArgumentException("User " + userId + " is not an ENGINEER");
        }
        return user;
    }

    private EngineerResponse toResponse(User user) {
        EngineerProfile profile = engineerProfileRepository.findByUserId(user.getId()).orElse(null);
        List<String> skills = engineerSkillRepository.findByUserId(user.getId()).stream()
                .map(EngineerSkill::getSkillName)
                .toList();

        return EngineerResponse.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .enabled(user.getEnabled())
                .primarySpecialization(profile != null ? profile.getPrimarySpecialization() : null)
                .activeTaskLimit(profile != null ? profile.getActiveTaskLimit() : 5)
                .skills(skills)
                .build();
    }

    private EngineerSkillResponse toSkillResponse(EngineerSkill skill) {
        return EngineerSkillResponse.builder()
                .id(skill.getId())
                .skillName(skill.getSkillName())
                .build();
    }
}