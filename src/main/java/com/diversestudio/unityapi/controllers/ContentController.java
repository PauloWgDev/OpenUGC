package com.diversestudio.unityapi.controllers;

import com.diversestudio.unityapi.dto.ContentCreationDTO;
import com.diversestudio.unityapi.dto.ContentDTO;
import com.diversestudio.unityapi.entities.Content;
import com.diversestudio.unityapi.entities.Download;
import com.diversestudio.unityapi.service.ContentService;
import com.diversestudio.unityapi.service.DownloadService;
import com.diversestudio.unityapi.storage.StorageService;
import com.diversestudio.unityapi.util.NativeQueryHelper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
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
    private final DownloadService downloadService;
    private final NativeQueryHelper nativeQueryHelper;

    @Value("${file.storage.type}")
    private String storageType;


    public ContentController(ContentService contentService, StorageService storageService, DownloadService downloadService,NativeQueryHelper nativeQueryHelper) {
        this.contentService = contentService;
        this.storageService = storageService;
        this.downloadService = downloadService;
        this.nativeQueryHelper = nativeQueryHelper;
    }


    // Helper Function to Extract IP address
    private String extractClientIp(HttpServletRequest request) {
        String header = request.getHeader("X-Forwarded-For");
        if (header == null || header.isEmpty()) {
            return request.getRemoteAddr();
        } else {
            return header.split(",")[0].trim(); // First IP in chain
        }
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
    public ResponseEntity<Page<ContentDTO>> getContentPage(
            @RequestParam(defaultValue = "") String prompt,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort,
            @RequestParam(required = false) Integer creatorId)
    {
        Sort sortOrder = nativeQueryHelper.StringToSort(sort);
        Pageable pageable = PageRequest.of(page, size, sortOrder);
        Page<ContentDTO> contentList = contentService.getContentPage(prompt, creatorId,pageable);
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
     * GET /api/content/download/{id} - Handles the download request for a content item by its unique identifier.
     *
     * @param id the unique identifier of the content to be downloaded
     * @param request the HTTP request, used to extract the client IP address
     * @return a {@link ResponseEntity} with the binary content of the file as an attachment,
     *         or a 404 response if the content is not found
     * @throws Exception if file loading or download registration fails
     */
    @GetMapping("/download/{id}")
    public ResponseEntity<?> downloadContent(@PathVariable Long id, HttpServletRequest request) throws Exception {
        Optional<ContentDTO> contentOptional = contentService.getContentById(id);
        if (contentOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        ContentDTO content = contentOptional.get();

        // Extract client IP and register download
        String ipAddress = extractClientIp(request);
        downloadService.registerDownload(content, ipAddress);

        String fileName = content.data(); // works for both local and cloud

        // Load resource from storage (local or cloud)
        Resource fileResource = storageService.loadFileAsResource(fileName);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header("Content-Disposition", "attachment; filename=\"" + fileName + "\"")
                .body(fileResource);
    }

    @Value("${file.storage.baseUrl}")
    private String fileBaseUrl;

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
        String fileUrl = "base-url not implemented";
        String thumbnailUrl = null;

        // if fileBaseUrl equals 'not_using' avoid calling storageService
        if (!fileBaseUrl.equals("not_using"))
        {
            fileUrl = storageService.uploadFile(file);

            // Upload the thumbnail if provided.
            if (thumbnail != null && !thumbnail.isEmpty()) {
                thumbnailUrl = storageService.uploadFile(thumbnail);
            }
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

    //TODO: There should be a soft Delete and Hard Delete endpoints
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
