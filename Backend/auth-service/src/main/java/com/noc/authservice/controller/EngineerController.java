package com.noc.authservice.controller;

import com.noc.authservice.dto.EngineerResponse;
import com.noc.authservice.dto.EngineerSkillResponse;
import com.noc.authservice.dto.SkillRequest;
import com.noc.authservice.dto.SpecializationRequest;
import com.noc.authservice.service.EngineerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Reachable through api-gateway at /api/engineers/**.
 * GET /api/engineers is the internal-facing list consumed by
 * incident-service's assignment engine via Feign (see SecurityConfig —
 * left reachable to any authenticated caller, all callers in this
 * project's scope are trusted services).
 *
 * "Me" endpoints identify the caller from the validated JWT
 * (SecurityContext), never from a client-supplied header.
 */
@RestController
@RequestMapping("/api/engineers")
@RequiredArgsConstructor
public class EngineerController {

    private final EngineerService engineerService;

    @GetMapping
    public ResponseEntity<List<EngineerResponse>> getEngineers() {
        return ResponseEntity.ok(engineerService.getEngineers());
    }

    @PutMapping("/{userId}/specialization")
    public ResponseEntity<EngineerResponse> setSpecialization(
            @PathVariable Long userId, @Valid @RequestBody SpecializationRequest request) {
        return ResponseEntity.ok(engineerService.setSpecialization(userId, request.getSpecialization()));
    }

    @GetMapping("/me/skills")
    public ResponseEntity<List<EngineerSkillResponse>> getMySkills(Authentication authentication) {
        return ResponseEntity.ok(engineerService.getMySkills(currentUserId(authentication)));
    }

    @PostMapping("/me/skills")
    public ResponseEntity<EngineerSkillResponse> addMySkill(
            Authentication authentication, @Valid @RequestBody SkillRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(engineerService.addMySkill(currentUserId(authentication), request.getSkillName()));
    }

    @DeleteMapping("/me/skills/{skillId}")
    public ResponseEntity<Void> deleteMySkill(
            Authentication authentication, @PathVariable Long skillId) {
        engineerService.deleteMySkill(currentUserId(authentication), skillId);
        return ResponseEntity.noContent().build();
    }

    private Long currentUserId(Authentication authentication) {
        return (Long) authentication.getDetails();
    }
}