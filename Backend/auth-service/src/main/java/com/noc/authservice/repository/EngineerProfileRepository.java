package com.noc.authservice.repository;

import com.noc.authservice.entity.EngineerProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EngineerProfileRepository extends JpaRepository<EngineerProfile, Long> {

    Optional<EngineerProfile> findByUserId(Long userId);

    boolean existsByUserId(Long userId);
}