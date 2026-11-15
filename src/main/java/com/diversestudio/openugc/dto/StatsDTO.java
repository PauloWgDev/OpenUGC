package com.diversestudio.openugc.dto;

public record StatsDTO(
        Long downloadsCount,
        Long latestDownloadsCount, // downloads made in the last 7 days (by download date)
        Long ratingCount,
        Double avgRating) { }
