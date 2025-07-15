package com.diversestudio.unityapi.service;

import com.diversestudio.unityapi.dto.ContentCreationDTO;
import com.diversestudio.unityapi.dto.ContentDTO;
import com.diversestudio.unityapi.entities.Content;
import com.diversestudio.unityapi.entities.ContentDates;
import com.diversestudio.unityapi.entities.Tag;
import com.diversestudio.unityapi.entities.User;
import com.diversestudio.unityapi.repository.ContentRepository;
import com.diversestudio.unityapi.repository.TagRepository;
import com.diversestudio.unityapi.repository.UserRepository;
import com.diversestudio.unityapi.security.AuthHelper;
import com.diversestudio.unityapi.storage.StorageService;
import com.diversestudio.unityapi.util.NativeQueryHelper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;

@Service
public class ContentService {
    @PersistenceContext
    private EntityManager entityManager;
    private final ContentRepository contentRepository;
    private final UserRepository userRepository;
    private final StorageService storageService;
    private final NativeQueryHelper nativeQueryHelper;

    public ContentService(ContentRepository contentRepository, UserRepository userRepository, StorageService storageService,NativeQueryHelper nativeQueryHelper){
        this.contentRepository = contentRepository;
        this.userRepository = userRepository;
        this.storageService = storageService;
        this.nativeQueryHelper = nativeQueryHelper;
    }

    private StringBuilder appendOrderAndPagination(String prompt, Pageable pageable, StringBuilder sqlBuilder)
    {
        // Now build dynamic ORDER BY.
        // If a sort is explicitly provided, use it.
        if (pageable.getSort().isSorted()) {
            sqlBuilder.append(" ORDER BY ");
            List<String> orderList = new ArrayList<>();
            pageable.getSort().forEach(order -> {
                String property = order.getProperty();
                String direction = order.getDirection().toString();
                // If sorting on avgRating, add NULLS LAST as before
                if ("avgRating".equals(property)) {
                    orderList.add(property + " " + direction + " NULLS LAST");
                } else {
                    orderList.add(property + " " + direction);
                }
            });
            sqlBuilder.append(String.join(", ", orderList));
        }
        // If no sort is provided but a prompt is present, sort by similarity.
        else if (prompt != null && !prompt.isEmpty()) {
            sqlBuilder.append(nativeQueryHelper.getOrderBySimilarity("c.name"));
        }
        // Otherwise, use default sort.
        else {
            sqlBuilder.append(" ORDER BY createdAt DESC");
        }

        // Append pagination.
        sqlBuilder.append(" LIMIT :limit OFFSET :offset");

        return sqlBuilder;
    }

    private Query buildNativeQueryWithParams(StringBuilder sqlBuilder, String prompt, Integer creatorId, List<String> tags, Pageable pageable)
    {
        // Create and set up the query.
        Query query = entityManager.createNativeQuery(sqlBuilder.toString(), "ContentDTOMapping");

        if (prompt != null && !prompt.isEmpty()) {
            query.setParameter("prompt", prompt);
        }
        if (creatorId != null) {
            query.setParameter("creatorId", creatorId);
        }
        if (tags != null && !tags.isEmpty())
        {
            query.setParameter("tags", tags);
        }

        query.setParameter("limit", pageable.getPageSize());
        query.setParameter("offset", pageable.getOffset());

        return query;
    }

    public Page<ContentDTO> getContentPage(String prompt, Integer creatorId, List<String> tags, Pageable pageable) {
        StringBuilder sqlBuilder = new StringBuilder(nativeQueryHelper.getFindAllContents());

        boolean hasPrompt = prompt != null && !prompt.isEmpty();
        boolean hasCreator = creatorId != null;
        boolean hasTags = tags != null && !tags.isEmpty();

        // Append WHERE clauses dynamically
        if (hasPrompt || hasCreator || hasTags) {
            sqlBuilder.append(" WHERE ");
            List<String> conditions = new ArrayList<>();

            if (hasPrompt) {
                conditions.add("c.name ILIKE CONCAT('%', :prompt, '%')");
            }
            if (hasCreator) {
                conditions.add("c.creator = :creatorId");
            }
            if (hasTags)
            {
                conditions.add("EXISTS (SELECT 1 FROM content_tag ct2 JOIN tags t2 ON ct2.tag_id = t2.tag_id WHERE ct2.content_id = c.content_id AND t2.name = ANY(:tags))");
            }

            sqlBuilder.append(String.join(" AND ", conditions));
        }

        // Order & Pagination
        appendOrderAndPagination(prompt, pageable, sqlBuilder);

        // Build Native Query
        Query query = buildNativeQueryWithParams(sqlBuilder, prompt, creatorId, tags, pageable);

        List<ContentDTO> results = query.getResultList();

        // Note: For production, might want to run a separate COUNT(*) query for total elements.
        long total = results.size();

        return new PageImpl<>(results, pageable, total);
    }

    public Optional<ContentDTO> getContentById(Long id) {
        String sql = nativeQueryHelper.getFindSingleContent();
        Query query = entityManager.createNativeQuery(sql, "ContentDTOMapping");
        query.setParameter("id", id);

        List<ContentDTO> results = query.getResultList(); // modify query to support thumbnail

        if (results.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(results.get(0));
        }
    }

    @Autowired
    private TagRepository tagRepository;

    private Set<Tag> processTags(List<String> tagNames) {
        Set<Tag> tags = new HashSet<>();
        for (String tagName : tagNames) {
            Tag tag = tagRepository.findByName(tagName.trim().toLowerCase())
                    .orElseGet(() -> {
                        Tag newTag = new Tag();
                        newTag.setName(tagName);
                        return newTag;
                    });
            tags.add(tag);
        }
        return tags;
    }


    @Transactional
    public Content createContent(ContentCreationDTO dto) {
        // Retrieve the authenticated user's ID
        Long creatorId = AuthHelper.getCurrentUserId();

        // Fetch the user from the database
        Optional<User> userOptional = userRepository.findById(creatorId);
        if (userOptional.isEmpty()) {
            throw new IllegalArgumentException("User with ID " + creatorId + " not found.");
        }

        // create Set<Tag> from list of strings tag in dto

        // Map DTO fields to the Content entity
        Content content = new Content();
        content.setName(dto.name());
        content.setDescription(dto.description());
        content.setData(dto.data());
        content.setThumbnail(dto.thumbnail());
        content.setVersion(dto.version());
        content.setCreator(userOptional.get());

        if (!dto.tags().isEmpty()) {
            content.setTags(processTags(dto.tags()));
        }

        // Create and set ContentDates
        ContentDates contentDates = new ContentDates();
        Timestamp now = Timestamp.from(Instant.now());
        contentDates.setCreatedAt(now);
        contentDates.setUpdatedAt(now);
        contentDates.setContent(content);
        content.setContentDates(contentDates);

        return contentRepository.save(content);
    }

    @Transactional
    public void deleteContent(Long contentId) throws Exception {
        // Retrieve the authenticated user's ID
        Long currentUserId = AuthHelper.getCurrentUserId();

        // Fetch the content record to delete
        Optional<Content> contentOptional = contentRepository.findById(contentId);
        if (!contentOptional.isPresent()) {
            throw new Exception("Content with ID " + contentId + " not found.");
        }

        Content content = contentOptional.get();

        //Check if the current user is authorized to delete this content.
        if (!content.getCreator().getUserId().equals(currentUserId)) {
            throw new Exception("Unauthorized deletion attempt.");
        }

        // Delete the main file from storage if it exists.
        if (content.getData() != null) {
            storageService.deleteFile(content.getData());
        }

        // Delete the thumbnail from storage if it exists.
        if (content.getThumbnail() != null) {
            storageService.deleteFile(content.getThumbnail());
        }

        // Finally, delete the content record from the repository.
        contentRepository.delete(content);
    }
}
