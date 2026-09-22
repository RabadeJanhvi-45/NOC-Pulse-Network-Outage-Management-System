package com.noc.incidentservice.client;

import com.noc.incidentservice.dto.EngineerResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

/**
 * Calls auth-service by its Eureka-registered application name
 * ("auth-service"). Used by the AssignmentEngine to fetch the current
 * Engineer roster (userId, primarySpecialization, skills, activeTaskLimit).
 */
@FeignClient(
        name = "auth-service",
        contextId = "authServiceClient"
)
public interface AuthServiceClient {

    @GetMapping("/api/engineers")
    List<EngineerResponse> getEngineers();
}