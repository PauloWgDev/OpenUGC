package com.diversestudio.unityapi.controllers;

import com.diversestudio.unityapi.entities.Rating;
import com.diversestudio.unityapi.service.RatingService;
import com.diversestudio.unityapi.util.NativeQueryHelper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/rating")
public class RatingController
{
    private final RatingService ratingService;
    private final NativeQueryHelper nativeQueryHelper;

    public RatingController(RatingService ratingService, NativeQueryHelper nativeQueryHelper) {
        this.ratingService = ratingService;
        this.nativeQueryHelper = nativeQueryHelper;
    }

    /**
     * POST /api/rating/{content_id} - Registers a user's rating for the specified content.
     * <p>
     * This endpoint extracts the content ID from the URL, accepts a rating value and an optional comment as request parameters,
     * verifies that the content exists, and then registers the rating associated with the currently authenticated user.
     * If the content is not found, an error is thrown.
     * </p>
     *
     * @param id      the unique identifier of the content to rate.
     * @param rating  the numeric rating provided by the user.
     * @param comment an optional comment about the content.
     * @return a {@link ResponseEntity} containing the saved {@link Rating} record.
     */
    @PostMapping("/{id}")
    public ResponseEntity<Rating> uploadRating(@PathVariable Long id,
                                               @RequestParam int rating,
                                               @RequestParam(required = false) String comment)
    {
        Rating savedRating = ratingService.registerRating(id, rating, comment);
        return ResponseEntity.ok(savedRating);
    }

    // Get /api/rating/{content_id} will return page of ratings associated with the given content id
    @GetMapping("/{id}")
    public ResponseEntity<Page<Rating>> getRatingPage(@PathVariable Long id,
                                                      @RequestParam(defaultValue = "0") int page,
                                                      @RequestParam(defaultValue = "10") int size,
                                                      @RequestParam(defaultValue = "createdAt,desc") String sort,
                                                      @RequestParam(defaultValue = "-1", required = false) Integer rating)
    {
        Sort sortOrder = nativeQueryHelper.StringToSort(sort);
        Pageable pageable = PageRequest.of(page, size, sortOrder);
        Page<Rating> ratingPage = ratingService.getRatingPage(id, rating, pageable);
        return ResponseEntity.ok(ratingPage);
    }
}
