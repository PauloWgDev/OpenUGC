package com.diversestudio.unityapi.repository;

import com.diversestudio.unityapi.dto.ContentDTO;
import com.diversestudio.unityapi.entities.Content;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ContentRepository extends JpaRepository<Content, Long> {

    // Custom query to join content and content_dates
    @Query(value = "SELECT " +
            " c.content_id AS contentId, " +
            " u.user_id AS creator, " +
            " u.username AS creatorName, " +
            " c.data, " +
            " c.name, " +
            " c.description, " +
            " c.version, " +
            " (SELECT COUNT(*) FROM downloads WHERE content_id = c.content_id) AS downloadsCount, " +
            " (SELECT COUNT(*) " +
            "    FROM downloads d " +
            "    JOIN content_dates cd ON d.content_id = cd.content_id " +
            "    WHERE d.content_id = c.content_id " +
            "      AND cd.created_at >= NOW() - INTERVAL '7 days') AS latestDownloadsCount, " +
            " (SELECT AVG(rating)::float FROM rating WHERE content_id = c.content_id) AS avgRating, " +
            " cd.created_at AS createdAt, " +
            " cd.updated_at AS updatedAt " +
            "FROM content c " +
            "JOIN content_dates cd ON c.content_id = cd.content_id " +
            "JOIN \"users\" u ON c.creator = u.user_id",
            nativeQuery = true)
    List<ContentDTO> findAllContentWithDates();


    @Query(value = "SELECT " +
            " c.content_id AS contentId, " +
            " u.user_id AS creatorId, " +
            " u.username AS creatorName, " +
            " c.data, " +
            " c.name, " +
            " c.description, " +
            " c.version, " +
            " (SELECT COUNT(*) FROM downloads WHERE content_id = c.content_id) AS downloadsCount, " +
            " (SELECT COUNT(*) " +
            "    FROM downloads d " +
            "    JOIN content_dates cd ON d.content_id = cd.content_id " +
            "    WHERE d.content_id = c.content_id " +
            "      AND cd.created_at >= NOW() - INTERVAL '7 days') AS latestDownloadsCount, " +
            " (SELECT AVG(rating)::float FROM rating WHERE content_id = c.content_id) AS avgRating, " +
            " cd.created_at AS createdAt, " +
            " cd.updated_at AS updatedAt " +
            "FROM content c " +
            "JOIN content_dates cd ON c.content_id = cd.content_id " +
            "JOIN users u ON c.creator = u.user_id " +
            "WHERE c.content_id = :id",
            nativeQuery = true)
    Optional<ContentDTO> findContentDTOById(@Param("id") Long id);

}
