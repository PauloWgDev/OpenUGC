package com.diversestudio.unityapi.service;

import com.diversestudio.unityapi.dto.ContentDTO;
import com.diversestudio.unityapi.entities.Rating;
import com.diversestudio.unityapi.repository.RatingRepository;
import com.diversestudio.unityapi.security.AuthHelper;
import com.diversestudio.unityapi.util.NativeQueryHelper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.PersistenceContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class RatingService {
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
     * @param ratingValue the rating value (e.g., between 1 and 5).
     * @param comment     an optional comment provided by the user.
     * @return the saved {@link Rating} record.
     * @throws EntityNotFoundException if the content is not found.
     */
    public Rating registerRating(Long contentId, float ratingValue, String comment) {
        // Verify if the content exists. This ensures that ratings are only
        // registered for existing content.
        Optional<ContentDTO> contentOptional = contentService.getContentById(contentId);
        if (contentOptional.isEmpty()) {
            throw new EntityNotFoundException("Content not found for id: " + contentId);
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
        StringBuilder sqlBuilder = new StringBuilder(nativeQueryHelper.getFindRatingsByContent());

        if (ratingFilter != null && ratingFilter >= 1 && ratingFilter <= 5) {
            sqlBuilder.append(" AND r.rating = :ratingFilter");
        }

        sqlBuilder.append(" LIMIT :limit OFFSET :offset");

        var query = entityManager.createNativeQuery(sqlBuilder.toString(), Rating.class);
        query.setParameter("contentId", contentId);
        query.setParameter("limit", pageable.getPageSize());
        query.setParameter("offset", pageable.getOffset());
        if (ratingFilter != null && ratingFilter >= 1 && ratingFilter <= 5) {
            query.setParameter("ratingFilter", ratingFilter);
        }

        @SuppressWarnings("unchecked")
        List<Rating> results = query.getResultList();

        //TODO: run a COUNT query for correct total.
        long total = results.size();

        return new PageImpl<>(results, pageable, total);
    }
}
