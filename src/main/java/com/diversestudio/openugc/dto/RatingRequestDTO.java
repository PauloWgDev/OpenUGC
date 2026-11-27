package com.diversestudio.openugc.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Body of POST /api/v1/rating/{id}: a whole number from 1 to 5 and an optional comment.
 */
public record RatingRequestDTO(
        @NotNull @Min(RatingRequestDTO.MIN_RATING) @Max(RatingRequestDTO.MAX_RATING) Integer rating,
        @Size(max = RatingRequestDTO.COMMENT_MAX) String comment
) {
    public static final int MIN_RATING = 1;
    public static final int MAX_RATING = 5;
    public static final int COMMENT_MAX = 2_000;
}
