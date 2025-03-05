package com.diversestudio.unityapi.repository;

import com.diversestudio.unityapi.dto.StatsResponse;
import com.diversestudio.unityapi.entities.Content;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface StatsRepository extends CrudRepository<Content, Long> {
    @Query(value = "SELECT " +
            " (SELECT COUNT(*) FROM downloads WHERE content_id = :contentId) AS downloadCount, " +
            " (SELECT COUNT(*) FROM rating WHERE content_id = :contentId) AS ratingCount, " +
            " (SELECT AVG(rating)::float FROM rating WHERE content_id = :contentId) AS avgRating " +
            "FROM content WHERE content_id = :contentId",
            nativeQuery = true)
    StatsResponse findStatsByContentId(@Param("contentId") Long contentId);
}
