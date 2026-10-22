package com.diversestudio.unityapi.dto;

import com.fasterxml.jackson.annotation.JsonAlias;

/**
 * Body of POST /api/auth/register.
 * <p>
 * Only these two fields are accepted. Anything else in the request (userId, role, joinedAt, ...)
 * is ignored, so a client can't overwrite an existing account or pick its own role.
 * "password" is accepted as an alias of "credential" to match the published docs.
 */
public record RegisterRequest(
        String username,
        @JsonAlias("password") String credential
) { }
