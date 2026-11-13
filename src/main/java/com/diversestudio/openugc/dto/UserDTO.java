package com.diversestudio.openugc.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.sql.Timestamp;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record UserDTO(
        Long userId,
        // Also the body of PATCH /api/users, where null means "leave unchanged".
        // @Pattern rejects a username made only of whitespace; null still passes.
        @Size(min = UserLimits.USERNAME_MIN, max = UserLimits.USERNAME_MAX)
        @Pattern(regexp = ".*\\S.*", message = "must not be blank") String username,
        Timestamp joinedAt,
        String role, // Role name
        @Size(max = 2048) String profilePicture,
        @Size(max = UserLimits.ABOUT_ME_MAX) String aboutMe,
        Integer downloadsPerformed,
        Integer downloadReceived,
        Float averageRatingsReceive
) { }
