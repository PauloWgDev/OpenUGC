package com.diversestudio.unityapi.service;

import com.diversestudio.unityapi.dto.ContentCreationDTO;
import com.diversestudio.unityapi.dto.ContentDTO;
import com.diversestudio.unityapi.entities.Content;
import com.diversestudio.unityapi.entities.ContentDates;
import com.diversestudio.unityapi.entities.User;
import com.diversestudio.unityapi.repository.ContentRepository;
import com.diversestudio.unityapi.repository.UserRepository;
import com.diversestudio.unityapi.util.NativeQueryHelper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ContentService {
    @PersistenceContext
    private EntityManager entityManager;
    private final ContentRepository contentRepository;
    private final UserRepository userRepository;
    private final NativeQueryHelper nativeQueryHelper;

    public Sort StringToSort(String s) {
        String[] sortParams = s.split(",");
        String property = sortParams[0];

        if (sortParams.length == 2 && "desc".equalsIgnoreCase(sortParams[1])) {
            return Sort.by(property).descending();
        }
        return Sort.by(property).ascending();
    }


    public ContentService(ContentRepository contentRepository, UserRepository userRepository, NativeQueryHelper nativeQueryHelper){
        this.contentRepository = contentRepository;
        this.userRepository = userRepository;
        this.nativeQueryHelper = nativeQueryHelper;
    }

    public Page<ContentDTO> getAllContent(String prompt, Pageable pageable) {
        StringBuilder sqlBuilder = new StringBuilder(nativeQueryHelper.getFindAllContents());

        // If a prompt is provided, decide how to incorporate it:
        if (prompt != null && !prompt.isEmpty()) {
            // If your base query doesn't already have a WHERE clause, add one:
            sqlBuilder.append(nativeQueryHelper.getWhereFilter());
        }

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
            sqlBuilder.append(nativeQueryHelper.getOrderBySimilarity());
        }
        // Otherwise, use default sort.
        else {
            sqlBuilder.append(" ORDER BY createdAt DESC");
        }

        // Append pagination.
        sqlBuilder.append(" LIMIT :limit OFFSET :offset");

        // Create and set up the query.
        Query query = entityManager.createNativeQuery(sqlBuilder.toString(), "ContentDTOMapping");
        if (prompt != null && !prompt.isEmpty()) {
            query.setParameter("prompt", prompt);
        }
        query.setParameter("limit", pageable.getPageSize());
        query.setParameter("offset", pageable.getOffset());

        List<ContentDTO> results = query.getResultList();

        // Note: For production, you might want to run a separate COUNT(*) query for total elements.
        long total = results.size();

        return new PageImpl<>(results, pageable, total);
    }

    public Optional<ContentDTO> getContentById(Long id) {
        String sql = nativeQueryHelper.getFindSingleContent();
        Query query = entityManager.createNativeQuery(sql, "ContentDTOMapping");
        query.setParameter("id", id);

        List<ContentDTO> results = query.getResultList();

        if (results.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(results.get(0));
        }
    }


    @Transactional
    public Content createContent(ContentCreationDTO dto) {
        // Retrieve the currently authenticated user's id from the SecurityContext
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userIdStr = authentication.getName();
        Long creatorId = Long.parseLong(userIdStr);


        // Fetch the user from the database
        Optional<User> userOptional = userRepository.findById(creatorId);
        if (userOptional.isEmpty()) {
            throw new IllegalArgumentException("User with ID " + creatorId + " not found.");
        }

        // Map DTO fields to the Content entity
        Content content = new Content();
        content.setName(dto.name());
        content.setDescription(dto.description());
        content.setData(dto.data());
        content.setVersion(dto.version());
        content.setCreator(userOptional.get());

        // Create and set ContentDates
        ContentDates contentDates = new ContentDates();
        Timestamp now = Timestamp.from(Instant.now());
        contentDates.setCreatedAt(now);
        contentDates.setUpdatedAt(now);
        contentDates.setContent(content);
        content.setContentDates(contentDates);

        return contentRepository.save(content);
    }
}
