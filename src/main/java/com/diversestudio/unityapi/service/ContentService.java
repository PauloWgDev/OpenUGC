package com.diversestudio.unityapi.service;

import com.diversestudio.unityapi.dto.ContentDTO;
import com.diversestudio.unityapi.repository.ContentRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ContentService {
    private final ContentRepository contentRepository;

    public ContentService(ContentRepository contentRepository){
        this.contentRepository = contentRepository;
    }

    public List<ContentDTO> getAllContent() {
        return contentRepository.findAllContentWithDates();
    }
}
