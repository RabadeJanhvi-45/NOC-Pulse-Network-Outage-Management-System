package com.noc.authservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Body for public POST /api/auth/register/engineer endpoint. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EngineerRegisterRequest {

    @NotBlank(message = "username is required")
    private String username;

    @NotBlank(message = "password is required")
    private String password;

    private String email;

    private String fullName;

    private String specialization;
}
