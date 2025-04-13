package com.diversestudio.unityapi.controllers;

import com.diversestudio.unityapi.dto.ContentCreationDTO;
import com.diversestudio.unityapi.dto.ContentDTO;
import com.diversestudio.unityapi.entities.Content;
import com.diversestudio.unityapi.service.ContentService;
import com.diversestudio.unityapi.storage.StorageService;
import com.diversestudio.unityapi.util.NativeQueryHelper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

@RestController
@RequestMapping("/api/content")
public class ContentController {
    private final ContentService contentService;
    private final StorageService storageService;
    private final NativeQueryHelper nativeQueryHelper;

    public ContentController(ContentService contentService, StorageService storageService, NativeQueryHelper nativeQueryHelper) {
        this.contentService = contentService;
        this.storageService = storageService;
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
     * @param creatorId if set only will return results of contents where this its creator is this creatorId
     * @return a {@link ResponseEntity} containing a page of {@link ContentDTO} objects
     */
    @GetMapping()
    public ResponseEntity<Page<ContentDTO>> getAllContent(
            @RequestParam(defaultValue = "") String prompt,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort,
            @RequestParam(required = false) Integer creatorId)
    {
        Sort sortOrder = nativeQueryHelper.StringToSort(sort);
        Pageable pageable = PageRequest.of(page, size, sortOrder);
        Page<ContentDTO> contentList = contentService.getAllContents(prompt, creatorId,pageable);
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
     * POST /api/content - Uploads a content file and an optional thumbnail, then creates a Content record.
     * <p>
     * Expects a multipart/form-data request with the following parts:
     * <ul>
     *     <li><strong>content</strong>: JSON payload mapping to {@link ContentCreationDTO}
     *         (excluding file URL and thumbnail URL).</li>
     *     <li><strong>file</strong>: The main file to be uploaded. Its public URL will be set as the {@code data} field.</li>
     *     <li><strong>thumbnail</strong> (optional): An optional thumbnail file to be uploaded. Its public URL
     *         will be set as the {@code thumbnail} field.</li>
     * </ul>
     * <p>
     * The endpoint first uploads the main file and, if provided, the thumbnail.
     * It then updates the received DTO with these URLs and creates the Content record.
     *
     * @param contentCreationDTO the content metadata (name, description, version, etc.)
     * @param file the main file to upload
     * @param thumbnail (optional) the thumbnail image file to upload
     * @return a ResponseEntity containing the created Content
     * @throws Exception if file upload or content creation fails
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Content> uploadContent(
            @RequestPart("content") ContentCreationDTO contentCreationDTO,
            @RequestPart("file") MultipartFile file,
            @RequestPart(value = "thumbnail", required = false) MultipartFile thumbnail) throws Exception {

        // Upload the main file and retrieve its public URL.
        String fileUrl = storageService.uploadFile(file);

        // Upload the thumbnail if provided.
        String thumbnailUrl = null;
        if (thumbnail != null && !thumbnail.isEmpty()) {
            thumbnailUrl = storageService.uploadFile(thumbnail);
        }

        // Create an updated DTO with file and thumbnail URLs.
        ContentCreationDTO updatedDTO = new ContentCreationDTO(
                contentCreationDTO.name(),
                contentCreationDTO.description(),
                contentCreationDTO.version(),
                fileUrl,      // Set file URL in 'data' field.
                thumbnailUrl, // Set thumbnail URL.
                contentCreationDTO.tags()
        );

        // Create the Content record using the updated DTO.
        Content savedContent = contentService.createContent(updatedDTO);
        return ResponseEntity.ok(savedContent);
    }

    /**
     * DELETE /api/content/{id} - Deletes a content record and its associated files.
     * <p>
     * This endpoint deletes the content record identified by the provided ID.
     * It also removes the main file (stored in the data field) and the optional thumbnail
     * from the storage service.
     *
     * @param id the ID of the content to delete
     * @return a ResponseEntity with no content (HTTP 204) if deletion is successful
     * @throws Exception if the content is not found or file deletion fails
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteContent(@PathVariable Long id) throws Exception {
        contentService.deleteContent(id);
        return ResponseEntity.noContent().build();
    }
}
