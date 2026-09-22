package com.noc.authservice.service;

import com.noc.authservice.dto.EngineerResponse;
import com.noc.authservice.dto.EngineerSkillResponse;

import java.util.List;

public interface EngineerService {

    List<EngineerResponse> getEngineers();

    EngineerResponse setSpecialization(Long userId, String specialization);

    List<EngineerSkillResponse> getMySkills(Long userId);

    EngineerSkillResponse addMySkill(Long userId, String skillName);

    void deleteMySkill(Long userId, Long skillId);
}