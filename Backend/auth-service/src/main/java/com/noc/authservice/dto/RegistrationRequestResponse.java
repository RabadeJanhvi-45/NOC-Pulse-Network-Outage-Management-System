package com.noc.authservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegistrationRequestResponse {

    private Long id;
    private Long userId;
    private String username;
    private String email;
    private String fullName;
    private String roleName;
    private String status;
    private String rejectionReason;
    private Long reviewedBy;
    private LocalDateTime requestedAt;
    private LocalDateTime reviewedAt;
}