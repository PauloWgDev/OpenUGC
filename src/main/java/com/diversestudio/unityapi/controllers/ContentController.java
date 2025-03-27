package com.diversestudio.unityapi.controllers;

import com.diversestudio.unityapi.dto.ContentCreationDTO;
import com.diversestudio.unityapi.dto.ContentDTO;
import com.diversestudio.unityapi.entities.Content;
import com.diversestudio.unityapi.service.ContentService;
import com.diversestudio.unityapi.util.NativeQueryHelper;
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
    private final NativeQueryHelper nativeQueryHelper;

    public ContentController(ContentService contentService, NativeQueryHelper nativeQueryHelper) {
        this.contentService = contentService;
        this.nativeQueryHelper = nativeQueryHelper;
    }

    /**
     * GET /api/content - Retrieves a paginated list of content entries, optionally filtered by prompt
     * and sorted according to a specified property and direction.
     *
     * @param prompt optional keyword to filter content by prompt (default: empty string, meaning no filter)
     * @param page the page number to retrieve (default: 0)
     * @param size the number of items per page (default: 10)
     * @param sort the sorting criteria in the format "property,direction" (e.g., "createdAt,desc")
     * @return a {@link ResponseEntity} containing a page of {@link ContentDTO} objects
     */
    @GetMapping()
    public ResponseEntity<Page<ContentDTO>> getAllContent(
            @RequestParam(defaultValue = "") String prompt,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort)
    {
        Sort sortOrder = nativeQueryHelper.StringToSort(sort);
        Pageable pageable = PageRequest.of(page, size, sortOrder);
        Page<ContentDTO> contentList = contentService.getAllContent(prompt, pageable);
        return ResponseEntity.ok(contentList);
    }

    /**
     * GET /api/content/{id} - Retrieves a specific content item by its ID.
     *
     * @param id the ID of the content to retrieve
     * @return a {@link ResponseEntity} containing an {@link Optional} {@link ContentDTO},
     * which may be empty if the content is not found
     */
    @GetMapping("/{id}")
    public ResponseEntity<Optional<ContentDTO>> getContent(@PathVariable Long id) {
        Optional<ContentDTO> content = contentService.getContentById(id);
        return ResponseEntity.ok(content);
    }

    /**
     * POST /api/content - Creates a new content item based on the provided data.
     *
     * @param contentCreationDTO the DTO containing content creation data
     * @return a {@link ResponseEntity} containing the saved {@link Content} entity
     */
    @PostMapping ResponseEntity<Content> createContent(@RequestBody ContentCreationDTO contentCreationDTO) {
        Content savedContent = contentService.createContent(contentCreationDTO);
        return ResponseEntity.ok(savedContent);
    }
}
