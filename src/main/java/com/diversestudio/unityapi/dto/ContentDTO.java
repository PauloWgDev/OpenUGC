package com.diversestudio.unityapi.dto;

import java.sql.Timestamp;

public record ContentDTO (
        Long contentId,
        Long creatorId,
        String creatorName,
        String data,
        String name,
        String description,
        int version,
        Timestamp createdAt,
        Timestamp updatedAt
){}
