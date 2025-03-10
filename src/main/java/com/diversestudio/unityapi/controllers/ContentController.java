package com.diversestudio.unityapi.controllers;

import com.diversestudio.unityapi.dto.ContentDTO;
import com.diversestudio.unityapi.entities.Content;
import com.diversestudio.unityapi.service.ContentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/content")
public class ContentController {
    private final ContentService contentService;

    public ContentController(ContentService contentService) {
        this.contentService = contentService;
    }

    // GET /api/content - Fetch all content with timestamps
    @GetMapping
    public ResponseEntity<List<ContentDTO>> getAllContent() {
        List<ContentDTO> contentList = contentService.getAllContent();
        return ResponseEntity.ok(contentList);
    }

    // POST api/content- Create new content
    @PostMapping ResponseEntity<Content> createContent(@RequestParam Long userId, @RequestBody Content content) {
        Content savedContent = contentService.createContent(userId, content);
        return ResponseEntity.ok(savedContent);
    }
}
