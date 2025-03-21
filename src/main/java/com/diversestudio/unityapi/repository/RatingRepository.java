package com.diversestudio.unityapi.repository;

import com.diversestudio.unityapi.entities.Rating;
import com.diversestudio.unityapi.entities.RatingId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface RatingRepository extends JpaRepository<Rating, RatingId> {

    @Query("SELECT COALESCE(AVG(r.rating), 0) FROM Rating r JOIN Content c ON r.contentId = c.contentId WHERE c.creator.userId = :userId")
    float averageRatingOfUserContent(@Param("userId") Long userId);
}
