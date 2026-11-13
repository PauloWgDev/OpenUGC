package com.diversestudio.openugc.service;

import com.diversestudio.openugc.dto.ContentCreationDTO;
import com.diversestudio.openugc.dto.ContentDTO;
import com.diversestudio.openugc.entity.Content;
import com.diversestudio.openugc.entity.ContentDates;
import com.diversestudio.openugc.entity.Tag;
import com.diversestudio.openugc.entity.User;
import com.diversestudio.openugc.exception.ResourceNotFoundException;
import com.diversestudio.openugc.exception.UnauthorizedActionException;
import com.diversestudio.openugc.repository.ContentRepository;
import com.diversestudio.openugc.repository.TagRepository;
import com.diversestudio.openugc.repository.UserRepository;
import com.diversestudio.openugc.security.AuthHelper;
import com.diversestudio.openugc.storage.StorageService;
import com.diversestudio.openugc.util.NativeQueryHelper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

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
    private static final Logger logger = LoggerFactory.getLogger(ContentService.class);

    // Whitelist of fields a client may sort content by: API name -> safe column/alias.
    private static final Map<String, String> CONTENT_SORT = Map.of(
            "createdAt",            "createdAt",
            "updatedAt",            "updatedAt",
            "name",                 "name",
            "version",              "version",
            "avgRating",            "avgRating",
            "downloadsCount",       "downloadsCount",
            "latestDownloadsCount", "latestDownloadsCount"
    );

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
                String column = nativeQueryHelper.resolveSortColumn(CONTENT_SORT, order.getProperty());
                String direction = order.getDirection().toString();
                // If sorting on avgRating, add NULLS LAST as before
                if ("avgRating".equals(column)) {
                    orderList.add(column + " " + direction + " NULLS LAST");
                } else {
                    orderList.add(column + " " + direction);
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

        logger.debug("Content query: {}", sqlBuilder);

        return sqlBuilder;
    }

    /**
     * Appends the list filters (soft delete, prompt, creator, tags) to a query whose FROM
     * aliases content as {@code c} and content_dates as {@code cd}. Shared by the page query
     * and its COUNT query, so the total always matches the filtered rows.
     */
    private void appendContentFilters(StringBuilder sql, String prompt, Integer creatorId, List<String> tags) {
        // Dummy 'Where'
        sql.append(" WHERE 1=1 ");
        sql.append(" AND cd.deleted_at IS NULL "); // filter out soft-deleted contents

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
    }

    /** Binds the parameters used by {@link #appendContentFilters}. */
    private void bindContentFilters(Query query, String prompt, Integer creatorId, List<String> tags) {
        if (prompt != null && !prompt.isEmpty()) {
            query.setParameter("prompt", prompt);
        }
        if (creatorId != null) {
            query.setParameter("creatorId", creatorId);
        }
        if (tags != null && !tags.isEmpty()) {
            query.setParameter("tags", tags);
        }
    }

    public Page<ContentDTO> getContentPage(String prompt, Integer creatorId, List<String> tags, Pageable pageable) {
        StringBuilder sql = new StringBuilder(nativeQueryHelper.getFindAllContents());
        appendContentFilters(sql, prompt, creatorId, tags);

        // GROUP BY
        sql.append(" ").append(nativeQueryHelper.getFindAllContentGroupBy());

        // ORDER & PAGINATION
        appendOrderAndPagination(prompt, pageable, sql);

        // Build & execute
        Query q = entityManager
                .createNativeQuery(sql.toString(), "ContentDTOMapping")
                .setParameter("limit", pageable.getPageSize())
                .setParameter("offset", pageable.getOffset());
        bindContentFilters(q, prompt, creatorId, tags);

        @SuppressWarnings("unchecked")
        List<ContentDTO> items = ((List<ContentDTO>) q.getResultList()).stream()
                .map(this::withApiUrls)
                .toList();

        // The COUNT query only runs when the total can't be worked out from this page alone.
        return PageableExecutionUtils.getPage(items, pageable, () -> countContent(prompt, creatorId, tags));
    }

    /** Number of content items matching the list filters, across all pages. */
    private long countContent(String prompt, Integer creatorId, List<String> tags) {
        StringBuilder sql = new StringBuilder(nativeQueryHelper.getCountContent());
        appendContentFilters(sql, prompt, creatorId, tags);

        Query count = entityManager.createNativeQuery(sql.toString());
        bindContentFilters(count, prompt, creatorId, tags);
        return ((Number) count.getSingleResult()).longValue();
    }

    /**
     * Returns a content item as exposed by the API: {@code data} and {@code thumbnail} are API URLs
     * (see {@link #withApiUrls}).
     */
    public Optional<ContentDTO> getContentById(Long id) {
        return findContentById(id).map(this::withApiUrls);
    }

    /**
     * Returns a content item with {@code data} and {@code thumbnail} as stored in the database
     * (storage keys), for loading the file from storage. Not meant to be returned to clients.
     */
    public Optional<ContentDTO> getContentForDownload(Long id) {
        return findContentById(id);
    }

    private Optional<ContentDTO> findContentById(Long id) {
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

    /** Replaces the stored keys in {@code data} and {@code thumbnail} with their public URLs. */
    /**
     * Replaces the storage keys in {@code data} and {@code thumbnail} with the API URLs that serve them:
     * {@code data} → {@code GET /api/content/download/{id}} (needs a token, counted as a download),
     * {@code thumbnail} → {@code GET /api/content/thumbnail/{id}} (public), or {@code null} if there is none.
     * Files are never linked directly, so local storage and GCS behave the same.
     */
    private ContentDTO withApiUrls(ContentDTO c) {
        return new ContentDTO(
                c.contentId(),
                c.creatorId(),
                c.creatorName(),
                downloadUrl(c.contentId()),
                thumbnailUrl(c.contentId(), c.thumbnail()),
                c.name(),
                c.description(),
                c.tags(),
                c.version(),
                c.downloadsCount(),
                c.latestDownloadsCount(),
                c.avgRating(),
                c.createdAt(),
                c.updatedAt()
        );
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

    /**
     * Absolute URL of {@code GET /api/content/download/{id}}, built from the current request.
     * Behind a reverse proxy the host and scheme come from the forwarded headers
     * ({@code server.forward-headers-strategy=native}).
     */
    private static String downloadUrl(Long contentId) {
        return ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/content/download/{id}")
                .buildAndExpand(contentId)
                .toUriString();
    }

    /** Absolute URL of {@code GET /api/content/thumbnail/{id}}, or {@code null} if the content has no thumbnail. */
    private static String thumbnailUrl(Long contentId, String thumbnailKey) {
        if (thumbnailKey == null || thumbnailKey.isBlank()) {
            return null;
        }
        return ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/content/thumbnail/{id}")
                .buildAndExpand(contentId)
                .toUriString();
    }

    /**
     * Maps a freshly created {@link Content} to the public {@link ContentDTO}.
     * <p>
     * Only the creator's id and username are copied, so no other user data
     * (credential hash, role, ...) can leak into the response.
     * A new item has no downloads or ratings yet, so those stats start at 0 / null.
     */
    private ContentDTO toNewContentDTO(Content content) {
        String[] tagNames = content.getTags().stream()
                .map(Tag::getName)
                .toArray(String[]::new);

        return new ContentDTO(
                content.getContentId(),
                content.getCreator().getUserId(),
                content.getCreator().getUsername(),
                downloadUrl(content.getContentId()),
                thumbnailUrl(content.getContentId(), content.getThumbnail()),
                content.getName(),
                content.getDescription(),
                tagNames,
                content.getVersion(),
                0L,   // downloadsCount
                0L,   // latestDownloadsCount
                null, // avgRating: no ratings yet
                content.getContentDates().getCreatedAt(),
                content.getContentDates().getUpdatedAt()
        );
    }
    

    @Transactional
    public ContentDTO createContent(ContentCreationDTO dto) {
        // Retrieve the authenticated user's ID
        Long creatorId = AuthHelper.getCurrentUserId();

        // Fetch the user from the database
        Optional<User> userOptional = userRepository.findById(creatorId);
        if (userOptional.isEmpty()) {
            throw new UnauthorizedActionException("Authenticated user no longer exists");
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

        Content saved = contentRepository.save(content);
        return toNewContentDTO(saved);
    }

    /**
     * Loads a content item and checks that the <b>current</b> user may modify it:
     * either they created it, or they are an ADMIN.
     * <p>
     * The role is read from the database rather than the JWT, so a demoted admin
     * loses moderation rights immediately instead of when their token expires.
     *
     * @throws ResourceNotFoundException   if the content does not exist (404)
     * @throws UnauthorizedActionException if the current user may not modify it (403)
     */
    private Content getContentIfAuthorized(Long contentId, String action) {
        Content content = contentRepository.findById(contentId)
                .orElseThrow(() -> new ResourceNotFoundException("Content not found"));

        Long currentUserId = AuthHelper.getCurrentUserId();
        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new UnauthorizedActionException("Authenticated user no longer exists"));

        boolean isOwner = content.getCreator().getUserId().equals(currentUserId);
        boolean isAdmin = "ADMIN".equals(currentUser.getRole().getRoleName());

        if (!isOwner && !isAdmin) {
            logger.warn("User {} tried to {} content {} owned by user {}",
                    currentUserId, action, contentId, content.getCreator().getUserId());
            throw new UnauthorizedActionException("User not authorized to " + action + " this content");
        }

        return content;
    }

    // Flags the content as deleted
    @Transactional
    public void softDeleteContent(Long contentId) throws Exception {
        Content content = getContentIfAuthorized(contentId, "soft delete");

        Timestamp now = Timestamp.from(Instant.now());
        content.getContentDates().setDeletedAt(now);
    }

    // Removes all data from the content
    @Transactional
    public void hardDeleteContent(Long contentId) throws Exception {
        Content content = getContentIfAuthorized(contentId, "hard delete");

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
