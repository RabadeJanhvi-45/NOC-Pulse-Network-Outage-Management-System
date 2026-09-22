package com.noc.authservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Request body for POST /api/users (create) and PUT /api/users/{id} (edit).
 * On create, username/password/roleId are required. On update, password is
 * optional — omit it to leave the existing password unchanged.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRequest {

    @NotBlank(message = "username is required")
    private String username;

    /** Required on create; optional on update (leave blank to keep current password). */
    private String password;

    private String email;

    private String fullName;

    @NotNull(message = "roleId is required")
    private Long roleId;

    private Boolean enabled;
}
