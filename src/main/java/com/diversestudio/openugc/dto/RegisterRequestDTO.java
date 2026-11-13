package com.diversestudio.openugc.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonAlias;

/**
 * Body of POST /api/auth/register.
 * <p>
 * Only these two fields are accepted. Anything else in the request (userId, role, joinedAt, ...)
 * is ignored, so a client can't overwrite an existing account or pick its own role.
 * "password" is accepted as an alias of "credential" to match the published docs.
 */
public record RegisterRequestDTO(
        @NotBlank @Size(min = UserLimits.USERNAME_MIN, max = UserLimits.USERNAME_MAX) String username,
        @JsonAlias("password")
        @NotBlank @Size(min = UserLimits.PASSWORD_MIN, max = UserLimits.PASSWORD_MAX) String credential
) { }
