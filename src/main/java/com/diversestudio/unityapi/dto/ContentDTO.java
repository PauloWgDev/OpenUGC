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
        Long downloadsCount, // total number of downloads
        Long latestDownloadsCount, // number of downloads in the last 7 days
        Double avgRating, // avg value off all ratings
        Timestamp createdAt,
        Timestamp updatedAt
){}
