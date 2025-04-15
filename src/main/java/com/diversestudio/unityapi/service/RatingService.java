package com.diversestudio.unityapi.service;

import com.diversestudio.unityapi.dto.ContentDTO;
import com.diversestudio.unityapi.entities.Rating;
import com.diversestudio.unityapi.repository.RatingRepository;
import com.diversestudio.unityapi.security.AuthHelper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class RatingService {
    @PersistenceContext
    private EntityManager entityManager;

    private final RatingRepository ratingRepository;
    private final ContentService contentService;

    public RatingService(RatingRepository ratingRepository,
                         ContentService contentService)
    {
        this.ratingRepository = ratingRepository;
        this.contentService = contentService;
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
}
