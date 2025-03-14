package com.diversestudio.unityapi.service;

import com.diversestudio.unityapi.dto.ContentCreationDTO;
import com.diversestudio.unityapi.dto.ContentDTO;
import com.diversestudio.unityapi.entities.Content;
import com.diversestudio.unityapi.entities.ContentDates;
import com.diversestudio.unityapi.entities.User;
import com.diversestudio.unityapi.repository.ContentRepository;
import com.diversestudio.unityapi.repository.UserRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
public class ContentService {
    private final ContentRepository contentRepository;
    private final UserRepository userRepository;

    public ContentService(ContentRepository contentRepository, UserRepository userRepository){
        this.contentRepository = contentRepository;
        this.userRepository = userRepository;
    }

    public List<ContentDTO> getAllContent() {
        return contentRepository.findAllContentWithDates();
    }

    public Optional<ContentDTO> getContentById(Long id) {
        return contentRepository.findContentDTOById(id);
    }


    @Transactional
    public Content createContent(ContentCreationDTO dto) {

        // Fetch the user from the database
        Optional<User> userOptional = userRepository.findById(dto.creatorId());
        if (userOptional.isEmpty()) {
            throw new IllegalArgumentException("User with ID " + dto.creatorId() + " not found.");
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
