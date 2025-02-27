package com.diversestudio.unityapi.dto;

import java.sql.Timestamp;

public record ContentDTO (
        Long contentId,
        String data,
        String name,
        String description,
        int version,
        Timestamp createdAt,
        Timestamp updatedAt
){}
