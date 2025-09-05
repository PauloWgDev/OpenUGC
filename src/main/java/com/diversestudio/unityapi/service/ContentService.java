package com.diversestudio.unityapi.service;

import ch.qos.logback.classic.Logger;
import com.diversestudio.unityapi.define.RoleName;
import com.diversestudio.unityapi.dto.ContentCreationDTO;
import com.diversestudio.unityapi.dto.ContentDTO;
import com.diversestudio.unityapi.entities.Content;
import com.diversestudio.unityapi.entities.ContentDates;
import com.diversestudio.unityapi.entities.Tag;
import com.diversestudio.unityapi.entities.User;
import com.diversestudio.unityapi.exeption.UnauthorizedActionException;
import com.diversestudio.unityapi.repository.ContentRepository;
import com.diversestudio.unityapi.repository.TagRepository;
import com.diversestudio.unityapi.repository.UserRepository;
import com.diversestudio.unityapi.security.AuthHelper;
import com.diversestudio.unityapi.storage.StorageService;
import com.diversestudio.unityapi.util.NativeQueryHelper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.slf4j.LoggerFactory;
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
    private static final Logger logger = (Logger) LoggerFactory.getLogger(ContentService.class);

    public ContentService(ContentRepository contentRepository, UserRepository userRepository, StorageService storageService,NativeQueryHelper nativeQueryHelper){
        this.contentRepository = contentRepository;
        this.userRepository = userRepository;
        this.storageService = storageService;
        this.nativeQueryHelper = nativeQueryHelper;
    }

    private StringBuilder appendOrderAndPagination(String prompt, Pageable pageable, StringBuilder sqlBuilder)
    {
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

        System.out.println("ORDER BY clause appended: " + sqlBuilder);

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
        StringBuilder sql = new StringBuilder(nativeQueryHelper.getFindAllContents());

        // Dummy 'Where'
        sql.append(" WHERE 1=1 ");

        if (prompt != null && !prompt.isEmpty()) {
            sql.append(" AND c.name ILIKE CONCAT('%', :prompt, '%') ");
        }
        if (creatorId != null) {
            sql.append(" AND c.creator = :creatorId ");
        }
        if (tags != null && !tags.isEmpty()) {
            sql.append(" AND EXISTS (")
                    .append(" SELECT 1")
                    .append(" FROM content_tag ct2")
                    .append(" JOIN tags t2 ON ct2.tag_id = t2.tag_id")
                    .append(" WHERE ct2.content_id = c.content_id")
                    .append(" AND t2.name IN (:tags)")
                    .append(") ");
        }

        // GROUP BY
        sql.append(" ").append(nativeQueryHelper.getFindAllContentGroupBy());

        // ORDER & PAGINATION
        appendOrderAndPagination(prompt, pageable, sql);

        // Build & execute
        Query q = entityManager
                .createNativeQuery(sql.toString(), "ContentDTOMapping")
                .setParameter("limit", pageable.getPageSize())
                .setParameter("offset", pageable.getOffset());

        if (prompt != null && !prompt.isEmpty())
            q.setParameter("prompt", prompt);
        if (creatorId != null)
            q.setParameter("creatorId", creatorId);
        if (tags != null && !tags.isEmpty())
            q.setParameter("tags", tags);

        List<ContentDTO> items = q.getResultList();
        long total = items.size();

        return new PageImpl<>(items, pageable, total);
    }

    public Optional<ContentDTO> getContentById(Long id) {
        StringBuilder sql = new StringBuilder(nativeQueryHelper.getFindSingleContent());

        // GROUP BY
        sql.append(" ").append(nativeQueryHelper.getFindAllContentGroupBy());


        Query query = entityManager.createNativeQuery(sql.toString(), "ContentDTOMapping");
        query.setParameter("id", id);

        List<ContentDTO> item = query.getResultList(); // modify query to support thumbnail

        if (item.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(item.get(0));
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

    private boolean isUserAuthorize(Long contentId) {
        Long currentUserId = AuthHelper.getCurrentUserId();

        Optional<Content> contentOptional = contentRepository.findById(contentId);
        if (contentOptional.isEmpty()) {
            logger.warn("Content with ID {} not found.", contentId);
            return false;
        }

        Content content = contentOptional.get();

        boolean isAuthorized = false;

        // Check if the current user is authorized to modify this content
        if (
                content.getCreator().getUserId().equals(currentUserId) ||
                content.getCreator().getRole().getRoleId() == RoleName.ADMIN.getId())
        {
            isAuthorized = true;
        }

        return isAuthorized;
    }

    // Flags the content as deleted
    @Transactional
    public void softDeleteContent(Long contentId) throws Exception {
        if (!isUserAuthorize(contentId)) {
            throw new UnauthorizedActionException("User not authorized to soft delete this content");
        }

        Optional<Content> contentOptional = contentRepository.findById(contentId);
        Content content = contentOptional.get();

        Timestamp now = Timestamp.from(Instant.now());
        content.getContentDates().setDeletedAt(now);
    }

    // Removes all data from the content
    @Transactional
    public void hardDeleteContent(Long contentId) throws Exception {
        if (!isUserAuthorize(contentId)) {
            throw new UnauthorizedActionException("User not authorized to hard delete this content");
        }
        Optional<Content> contentOptional = contentRepository.findById(contentId);
        Content content = contentOptional.get();

        // Delete the main file from storage if it exists.
        if (content.getData() != null) {
            storageService.deleteFile(content.getData());
        }

        // Delete the thumbnail from storage if it exists.
        if (content.getThumbnail() != null) {
            storageService.deleteFile(content.getThumbnail());
        }

        // Delete the content record from the repository.
        contentRepository.delete(content);
    }
}
