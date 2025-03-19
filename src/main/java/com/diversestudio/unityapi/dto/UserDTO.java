package com.diversestudio.unityapi.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.sql.Timestamp;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record UserDTO(
        Long userId,
        String username,
        Timestamp joinedAt,
        String role, // Role name
        String profilePicture,
        String aboutMe,
        int downloadsPerformed,
        int downloadReceived,
        float averageRatingsReceive
) { }
