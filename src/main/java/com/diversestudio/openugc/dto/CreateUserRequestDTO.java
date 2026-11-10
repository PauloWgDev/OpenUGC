package com.diversestudio.openugc.dto;

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
        String username,
        @JsonAlias("password") String credential,
        String role
) { }
