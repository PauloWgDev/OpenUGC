package com.diversestudio.unityapi.controllers;

import com.diversestudio.unityapi.dto.ContentCreationDTO;
import com.diversestudio.unityapi.dto.ContentDTO;
import com.diversestudio.unityapi.entities.Content;
import com.diversestudio.unityapi.service.ContentService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/content")
public class ContentController {
    private final ContentService contentService;

    public ContentController(ContentService contentService) {
        this.contentService = contentService;
    }

    // GET /api/content - Fetch all content with timestamps
    @GetMapping()
    public ResponseEntity<Page<ContentDTO>> getAllContent(
            @RequestParam(defaultValue = "") String prompt,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort)
    {
        Sort sortOrder = contentService.StringToSort(sort);
        Pageable pageable = PageRequest.of(page, size, sortOrder);
        Page<ContentDTO> contentList = contentService.getAllContent(prompt, pageable);
        return ResponseEntity.ok(contentList);
    }

    // GET /api/content/{id} - fetch a specific content based on its id
    @GetMapping("/{id}")
    public ResponseEntity<Optional<ContentDTO>> getContent(@PathVariable Long id) {
        Optional<ContentDTO> content = contentService.getContentById(id);
        return ResponseEntity.ok(content);
    }

    // POST api/content- Create new content
    @PostMapping ResponseEntity<Content> createContent(@RequestBody ContentCreationDTO contentCreationDTO) {
        Content savedContent = contentService.createContent(contentCreationDTO);
        return ResponseEntity.ok(savedContent);
    }
}
