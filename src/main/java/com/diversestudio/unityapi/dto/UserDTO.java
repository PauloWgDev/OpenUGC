package com.diversestudio.unityapi.dto;

import java.sql.Timestamp;

public record UserDTO(
        Long userId,
        String username,
        Timestamp joinedAt,
        String role // Role name
) { }
