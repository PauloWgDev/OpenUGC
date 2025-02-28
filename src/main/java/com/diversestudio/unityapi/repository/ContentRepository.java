package com.diversestudio.unityapi.repository;

import com.diversestudio.unityapi.dto.ContentDTO;
import com.diversestudio.unityapi.entities.Content;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContentRepository extends JpaRepository<Content, Long> {

    // Custom query to join content and content_dates
    @Query("""
        SELECT new com.diversestudio.unityapi.dto.ContentDTO(
            c.content_id, u.user_id, u.username,c.data, c.name, c.description, c.version,
            d.created_at, d.updated_at
        )
        FROM Content c
        JOIN ContentDates d ON c.content_id = d.content_id
        JOIN User u ON c.creator.user_id = u.user_id
    """)
    List<ContentDTO> findAllContentWithDates();
}
