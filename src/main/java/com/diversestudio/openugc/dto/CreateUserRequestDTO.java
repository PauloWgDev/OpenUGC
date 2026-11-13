package com.diversestudio.openugc.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonAlias;

/**
 * Body of POST /api/admin/users.
 * <p>
 * Lets an admin create an account directly. Only these fields are accepted, so a request
 * can't set userId (which would overwrite an existing account) or joinedAt.
 * "password" is accepted as an alias of "credential", same as on register.
 *
 * @param role optional role name (ADMIN, USER or GUEST); defaults to USER when omitted
 */
public record CreateUserRequestDTO(
        @NotBlank @Size(min = UserLimits.USERNAME_MIN, max = UserLimits.USERNAME_MAX) String username,
        @JsonAlias("password")
        @NotBlank @Size(min = UserLimits.PASSWORD_MIN, max = UserLimits.PASSWORD_MAX) String credential,
        String role
) { }
