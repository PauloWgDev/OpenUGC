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

    /**
     * GET /api/rating/{id} - Retrieves a paginated list of ratings for the specified content.
     * <p>
     * This endpoint allows clients to fetch a page of rating entries associated with a specific content ID.
     * Pagination and sorting parameters can be specified through query parameters.
     * Optionally, clients can filter ratings by a specific rating value.
     * </p>
     *
     * @param id      the unique identifier of the content for which ratings are to be fetched.
     * @param page    the page number to retrieve (0-based index). Defaults to 0 if not specified.
     * @param size    the number of items per page. Defaults to 10 if not specified.
     * @param sort    the sorting criteria, in the format "field,direction" (e.g., "createdAt,desc").
     *                Defaults to "createdAt,desc".
     * @param rating  an optional rating filter; if set to a value from 1 to 5, only ratings with that score will be returned.
     *                Use -1 to fetch all ratings. Defaults to -1.
     * @return a {@link ResponseEntity} containing a {@link Page} of {@link Rating} entities matching the criteria.
     */
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
