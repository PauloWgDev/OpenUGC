package com.diversestudio.unityapi.util.quearies;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Profile("postgres")
@Component
public class PostgresQueries implements QueryProvider {
    @Override
    public String getFindAllContent() {
        return "SELECT " +
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
                "JOIN \"users\" u ON c.creator = u.user_id";
    }

    @Override
    public String getFindSingleContent()
    {
        return
                "SELECT " +
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
                        "WHERE c.content_id = :id";
    }
}
