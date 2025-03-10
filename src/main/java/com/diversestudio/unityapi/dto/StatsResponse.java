package com.diversestudio.unityapi.dto;

public record StatsResponse(
        Long downloadsCount,
        Long latestDownloadsCount, // downloads in the last 7 days
        Long ratingCount,
        Double avgRating) { }
