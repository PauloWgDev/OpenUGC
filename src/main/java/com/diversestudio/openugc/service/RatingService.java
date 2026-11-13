package com.diversestudio.openugc.service;

import com.diversestudio.openugc.dto.ContentDTO;
import com.diversestudio.openugc.entity.Rating;
import com.diversestudio.openugc.exception.InvalidRequestException;
import com.diversestudio.openugc.exception.ResourceNotFoundException;
import com.diversestudio.openugc.repository.RatingRepository;
import com.diversestudio.openugc.security.AuthHelper;
import com.diversestudio.openugc.util.NativeQueryHelper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.data.domain.Page;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class RatingService {
    public static final int MIN_RATING = 1;
    public static final int MAX_RATING = 5;

    @PersistenceContext
    private EntityManager entityManager;
    private final RatingRepository ratingRepository;
    private final ContentService contentService;
    private final NativeQueryHelper nativeQueryHelper;

    public RatingService(RatingRepository ratingRepository,
                         ContentService contentService,
                         NativeQueryHelper nativeQueryHelper)
    {
        this.ratingRepository = ratingRepository;
        this.contentService = contentService;
        this.nativeQueryHelper = nativeQueryHelper;
    }


    /**
     * Registers a rating for a specified content.
     *
     * @param contentId   the ID of the content being rated.
     * @param ratingValue the rating value, from {@value #MIN_RATING} to {@value #MAX_RATING}.
     * @param comment     an optional comment provided by the user.
     * @return the saved {@link Rating} record.
     * @throws InvalidRequestException   if the rating is outside the allowed range (400).
     * @throws ResourceNotFoundException if the content is not found (404).
     */
    public Rating registerRating(Long contentId, float ratingValue, String comment) {
        // Written as !(in range) so NaN is rejected too.
        if (!(ratingValue >= MIN_RATING && ratingValue <= MAX_RATING)) {
            throw new InvalidRequestException(
                    "Rating must be between " + MIN_RATING + " and " + MAX_RATING + ", got " + ratingValue);
        }

        // Verify if the content exists. This ensures that ratings are only
        // registered for existing content.
        Optional<ContentDTO> contentOptional = contentService.getContentById(contentId);
        if (contentOptional.isEmpty()) {
            throw new ResourceNotFoundException("Content not found for id: " + contentId);
        }

        Long currentUserId = AuthHelper.getCurrentUserId();

        // Create and populate the new Rating entity.
        Rating newRating = new Rating();
        newRating.setContentId(contentId);
        newRating.setUserId(currentUserId);
        newRating.setRating(ratingValue);
        newRating.setComment(comment);

        // Save and return the new rating.
        return ratingRepository.save(newRating);
    }

    //TODO: implement the sorting

    // rating default value could be -1, and when rating is -1 it will not filter by rating value
    // if rating has a valid value (1 to 5) it will filter and only show ratings with that value.
    public Page<Rating> getRatingPage(Long contentId, Integer ratingFilter, Pageable pageable) {
        boolean filterByRating = ratingFilter != null && ratingFilter >= MIN_RATING && ratingFilter <= MAX_RATING;
        StringBuilder sqlBuilder = new StringBuilder(nativeQueryHelper.getFindRatingsByContent());

        if (filterByRating) {
            sqlBuilder.append(" AND r.rating = :ratingFilter");
        }

        sqlBuilder.append(" LIMIT :limit OFFSET :offset");

        var query = entityManager.createNativeQuery(sqlBuilder.toString(), Rating.class);
        query.setParameter("contentId", contentId);
        query.setParameter("limit", pageable.getPageSize());
        query.setParameter("offset", pageable.getOffset());
        if (filterByRating) {
            query.setParameter("ratingFilter", ratingFilter);
        }

        @SuppressWarnings("unchecked")
        List<Rating> results = query.getResultList();

        // The COUNT query only runs when the total can't be worked out from this page alone.
        return PageableExecutionUtils.getPage(results, pageable, () -> {
            StringBuilder countSql = new StringBuilder(nativeQueryHelper.getCountRatingsByContent());
            if (filterByRating) {
                countSql.append(" AND r.rating = :ratingFilter");
            }
            var count = entityManager.createNativeQuery(countSql.toString());
            count.setParameter("contentId", contentId);
            if (filterByRating) {
                count.setParameter("ratingFilter", ratingFilter);
            }
            return ((Number) count.getSingleResult()).longValue();
        });
    }


    public Map<Integer, Long> getRatingDistribution(Long contentId) {
        String sql = nativeQueryHelper.getRatingDistributionByContent();

        @SuppressWarnings("unchecked")
        List<Object[]> results = entityManager
                .createNativeQuery(sql)
                .setParameter("contentId", contentId)
                .getResultList();

        Map<Integer, Long> distribution = new LinkedHashMap<>();
        for (int i = 1; i <= 5; i++) {
            distribution.put(i, 0L); // default to 0 for all stars
        }

        for (Object[] row : results) {
            int stars = ((Number) row[0]).intValue();
            long count = ((Number) row[1]).longValue();
            distribution.put(stars, count);
        }

        return distribution;
    }

}
