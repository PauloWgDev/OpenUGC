package com.diversestudio.unityapi.repository;

import com.diversestudio.unityapi.dto.StatsDTO;
import com.diversestudio.unityapi.entities.Content;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface StatsRepository extends CrudRepository<Content, Long> {
    @Query(value = "SELECT " +
            " (SELECT COUNT(*) FROM downloads WHERE content_id = :contentId) AS downloadsCount, " +
            " (SELECT COUNT(*) FROM downloads d " +
            "    JOIN content_dates cd ON d.content_id = cd.content_id " +
            "    WHERE d.content_id = :contentId " +
            "      AND cd.created_at >= NOW() - INTERVAL '7 days') AS latestDownloadsCount, " +
            " (SELECT COUNT(*) FROM rating WHERE content_id = :contentId) AS ratingCount, " +
            " (SELECT AVG(rating)::float FROM rating WHERE content_id = :contentId) AS avgRating " +
            "FROM content " +
            "WHERE content_id = :contentId",
            nativeQuery = true)
    StatsDTO findStatsByContentId(@Param("contentId") Long contentId);
}
