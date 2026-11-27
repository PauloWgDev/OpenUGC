package com.diversestudio.openugc.controller;

import com.diversestudio.openugc.config.ApiPaths;
import com.diversestudio.openugc.dto.RatingRequestDTO;
import jakarta.validation.Valid;
import com.diversestudio.openugc.entity.Rating;
import com.diversestudio.openugc.service.RatingService;
import com.diversestudio.openugc.util.PageRequests;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping(ApiPaths.V1 + "/rating")
public class RatingController
{
    private final RatingService ratingService;
    private final PageRequests pageRequests;

    public RatingController(RatingService ratingService, PageRequests pageRequests) {
        this.ratingService = ratingService;
        this.pageRequests = pageRequests;
    }

    /**
     * POST /api/v1/rating/{content_id} - Registers a user's rating for the specified content.
     * <p>
     * The JSON body has the rating (a whole number from 1 to 5) and an optional comment
     * (up to {@value RatingRequestDTO#COMMENT_MAX} characters); anything else returns 400.
     * Rating again replaces the user's previous rating. If the content is not found, 404 is returned.
     * </p>
     *
     * @param id      the unique identifier of the content to rate.
     * @param request the rating and optional comment.
     * @return a {@link ResponseEntity} containing the saved {@link Rating} record.
     */
    @PostMapping("/{id}")
    public ResponseEntity<Rating> uploadRating(@PathVariable Long id,
                                               @Valid @RequestBody RatingRequestDTO request)
    {
        Rating savedRating = ratingService.registerRating(id, request.rating(), request.comment());
        return ResponseEntity.ok(savedRating);
    }

    /**
     * GET /api/v1/rating/{id} - Retrieves a paginated list of ratings for the specified content.
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
     *                Fields: createdAt, updatedAt, rating; anything else returns 400. Defaults to
     *                "createdAt,desc" (newest first).
     * @param rating  an optional rating filter; if set to a value from 1 to 5, only ratings with that score will be returned.
     *                Use -1 to fetch all ratings. Defaults to -1.
     * @return a {@link ResponseEntity} containing a {@link PagedModel} of {@link Rating} entities matching the criteria.
     */
    @GetMapping("/{id}")
    public ResponseEntity<PagedModel<Rating>> getRatingPage(@PathVariable Long id,
                                                      @RequestParam(defaultValue = "0") int page,
                                                      @RequestParam(defaultValue = "10") int size,
                                                      @RequestParam(defaultValue = "createdAt,desc") String sort,
                                                      @RequestParam(defaultValue = "-1", required = false) Integer rating)
    {
        Pageable pageable = pageRequests.of(page, size, sort);
        Page<Rating> ratingPage = ratingService.getRatingPage(id, rating, pageable);
        return ResponseEntity.ok(new PagedModel<>(ratingPage));
    }



    /**
    * GET /api/v1/rating/{id}/distribution - Retrieves the distribution of ratings for the specified content.
    * <p>
    * This endpoint returns a map where the keys represent rating values (typically from 1 to 5),
    * and the values indicate how many users gave each rating. This allows clients to visualize or analyze
    * the spread of user ratings for a particular content item.
    * </p>
    *
    * @param id the unique identifier of the content for which the rating distribution is to be fetched.
    * @return a {@link ResponseEntity} containing a {@link Map} where the key is the rating value
    *         (e.g., 1 to 5) and the value is the count of how many users gave that rating.
    */
    @GetMapping("/{id}/distribution")
    public ResponseEntity<Map<Integer, Long>> getRatingDistribution(@PathVariable Long id) {
        Map<Integer, Long> distribution = ratingService.getRatingDistribution(id);
        return ResponseEntity.ok(distribution);
    }
}
