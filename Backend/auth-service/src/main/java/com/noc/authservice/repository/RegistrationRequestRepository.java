package com.noc.authservice.repository;

import com.noc.authservice.entity.RegistrationRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RegistrationRequestRepository extends JpaRepository<RegistrationRequest, Long> {

    Optional<RegistrationRequest> findByUserId(Long userId);

    List<RegistrationRequest> findByStatus(String status);
}