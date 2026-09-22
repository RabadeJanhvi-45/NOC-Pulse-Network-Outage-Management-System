package com.noc.authservice.repository;

import com.noc.authservice.entity.EngineerSkill;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EngineerSkillRepository extends JpaRepository<EngineerSkill, Long> {

    List<EngineerSkill> findByUserId(Long userId);

    long countByUserId(Long userId);

    boolean existsByUserIdAndSkillNameIgnoreCase(Long userId, String skillName);

    void deleteByIdAndUserId(Long id, Long userId);
}