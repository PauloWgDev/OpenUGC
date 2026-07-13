package com.diversestudio.unityapi.dto;

public record StatsDTO(
        Long downloadsCount,
        Long latestDownloadsCount, // downloads in the last 7 days
        Long ratingCount,
        Double avgRating) { }
