package com.diversestudio.openugc.controller;

import jakarta.validation.Valid;

import com.diversestudio.openugc.dto.ContentCreationDTO;
import com.diversestudio.openugc.dto.ContentDTO;
import com.diversestudio.openugc.entity.User;
import com.diversestudio.openugc.exception.ResourceNotFoundException;
import com.diversestudio.openugc.exception.UnauthorizedActionException;
import com.diversestudio.openugc.repository.UserRepository;
import com.diversestudio.openugc.security.AuthHelper;
import com.diversestudio.openugc.service.ContentService;
import com.diversestudio.openugc.service.DownloadService;
import com.diversestudio.openugc.service.PermissionsService;
import com.diversestudio.openugc.storage.ImageType;
import com.diversestudio.openugc.storage.StorageService;
import com.diversestudio.openugc.storage.StoredFileNames;
import com.diversestudio.openugc.storage.UploadValidator;
import com.diversestudio.openugc.util.NativeQueryHelper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/content")
public class ContentController {
    private final ContentService contentService;
    private final StorageService storageService;
    private final DownloadService downloadService;
    private final NativeQueryHelper nativeQueryHelper;
    private final PermissionsService permissionsService;
    private final UserRepository userRepository;

    @Value("${file.storage.type}")
    private String storageType;

    public ContentController(ContentService contentService, StorageService storageService, DownloadService downloadService,NativeQueryHelper nativeQueryHelper, PermissionsService permissionsService, UserRepository userRepository) {
        this.contentService = contentService;
        this.storageService = storageService;
        this.downloadService = downloadService;
        this.nativeQueryHelper = nativeQueryHelper;
        this.permissionsService = permissionsService;
        this.userRepository = userRepository;
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
            @RequestParam(required = false) Integer creatorId,
            @RequestParam(required = false) List<String> tags)
    {
        Sort sortOrder = nativeQueryHelper.StringToSort(sort);
        Pageable pageable = PageRequest.of(page, size, sortOrder);
        Page<ContentDTO> contentList = contentService.getContentPage(prompt, creatorId, tags, pageable);
        return ResponseEntity.ok(contentList);
    }

    /**
     * GET /api/content/{id} - Retrieves a specific content item by its ID.
     *
     * @param id the ID of the content to retrieve
     * @return HTTP 200 with the {@link ContentDTO}, or 404 if no content has this id
     */
    @GetMapping("/{id}")
    public ResponseEntity<ContentDTO> getContent(@PathVariable Long id) {
        ContentDTO content = contentService.getContentById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Content not found for id: " + id));
        return ResponseEntity.ok(content);
    }

    /**
     * GET /api/content/download/{id} - Handles the download request for a content item by its unique identifier.
     *
     * @param id the unique identifier of the content to be downloaded
     * @param request the HTTP request; {@code getRemoteAddr()} gives the client IP
     * @return a {@link ResponseEntity} with the binary content of the file as an attachment,
     *         or a 404 response if the content is not found
     * @throws Exception if file loading or download registration fails
     */
    @GetMapping("/download/{id}")
    public ResponseEntity<Resource> downloadContent(@PathVariable Long id, HttpServletRequest request) throws Exception {
        ContentDTO content = contentService.getContentForDownload(id)
                .orElseThrow(() -> new ResourceNotFoundException("Content not found for id: " + id));

        // Load the file first, so a download is only counted if the file actually exists.
        Resource fileResource = storageService.loadFileAsResource(content.data());

        downloadService.registerDownload(content, request.getRemoteAddr());

        // Offer "<content name>.<stored extension>"; ContentDisposition escapes and encodes it.
        ContentDisposition disposition = ContentDisposition.attachment()
                .filename(StoredFileNames.downloadName(content.name(), content.data()), StandardCharsets.UTF_8)
                .build();

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .body(fileResource);
    }

    @Value("${file.storage.baseUrl}")
    private String fileBaseUrl;

    @Value("${file.cloud.baseUrl}")
    private String fileBaseUrl_cloud;

    /**
     * POST /api/content - Uploads a content file and an optional thumbnail, then creates a Content record.
     * <p>
     * Expects a multipart/form-data request with the following parts:
     * <ul>
     *     <li><strong>content</strong>: JSON payload mapping to {@link ContentCreationDTO}
     *         (name, description, version, tags). The {@code data} and {@code thumbnail} fields
     *         are ignored, because the server sets them after uploading the files.</li>
     *     <li><strong>file</strong>: The main file to be uploaded (any type, must not be empty).</li>
     *     <li><strong>thumbnail</strong> (optional): A PNG, JPEG or WebP image, checked by content.</li>
     * </ul>
     * Each part is limited by {@code spring.servlet.multipart.max-file-size} (1MB by default).
     * <p>
     * The endpoint checks that the authenticated user is allowed to post, validates the parts, uploads the
     * main file and, if provided, the thumbnail, then creates the Content record. Files are stored under
     * server-generated keys (see {@link StoredFileNames}): the client's file name is never used as a path,
     * so uploads can't overwrite each other or escape the storage location. The response carries public URLs.
     * If both storage base URLs are set to {@code not_using}, no files are uploaded and {@code data} is set
     * to a placeholder.
     * <p>
     * The response is a {@link ContentDTO}, the same shape returned by {@code GET /api/content/{id}}.
     * It exposes only the creator's id and username, never the full user entity. Because the content
     * is new, {@code downloadsCount} and {@code latestDownloadsCount} are 0 and {@code avgRating} is null.
     *
     * @param contentCreationDTO the content metadata (name, description, version, tags)
     * @param file the main file to upload
     * @param thumbnail (optional) the thumbnail image file to upload
     * @return HTTP 200 with the created content as a {@link ContentDTO};
     *         HTTP 400 if {@code file} is empty or {@code thumbnail} is not a supported image;
     *         HTTP 403 if the user is not allowed to post (e.g. a guest when {@code guest.can-post=false})
     * @throws Exception if file upload or content creation fails
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ContentDTO> uploadContent(
            @Valid @RequestPart("content") ContentCreationDTO contentCreationDTO,
            @RequestPart("file") MultipartFile file,
            @RequestPart(value = "thumbnail", required = false) MultipartFile thumbnail) throws Exception {

        Long currentUserId = AuthHelper.getCurrentUserId();
        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new UnauthorizedActionException("Authenticated user no longer exists"));

        if (!permissionsService.canPost(user)) {
            throw new UnauthorizedActionException("This account is not allowed to upload content");
        }

        // Validate every part before anything is written, so a bad thumbnail can't leave an orphaned file.
        UploadValidator.requireNonEmpty(file, "file");
        ImageType thumbnailType = (thumbnail != null && !thumbnail.isEmpty())
                ? UploadValidator.requireImage(thumbnail, "thumbnail")
                : null;

        String fileKey = "base-url not implemented";
        String thumbnailKey = null;
        List<String> storedKeys = new ArrayList<>(); // removed again if a later step fails

        try {
            // if fileBaseUrl equals 'not_using' avoid calling storageService
            if (!fileBaseUrl.equals("not_using") || !fileBaseUrl_cloud.equals("not_using"))
            {
                // Files are stored under server-generated keys; the client's file name only supplies the extension.
                fileKey = StoredFileNames.newKey(file.getOriginalFilename());
                storageService.uploadFile(fileKey, file, MediaType.APPLICATION_OCTET_STREAM_VALUE);
                storedKeys.add(fileKey);

                if (thumbnailType != null) {
                    thumbnailKey = StoredFileNames.newKeyWithExtension(thumbnailType.extension());
                    storageService.uploadFile(thumbnailKey, thumbnail, thumbnailType.contentType());
                    storedKeys.add(thumbnailKey);
                }
            }

            // Create an updated DTO with the storage keys.
            ContentCreationDTO updatedDTO = new ContentCreationDTO(
                    contentCreationDTO.name(),
                    contentCreationDTO.description(),
                    contentCreationDTO.version(),
                    fileKey,      // Storage key of the main file in 'data'.
                    thumbnailKey, // Storage key of the thumbnail.
                    contentCreationDTO.tags()
            );

            // Create the Content record using the updated DTO.
            ContentDTO savedContent = contentService.createContent(updatedDTO);
            return ResponseEntity.ok(savedContent);
        } catch (Exception e) {
            // Don't leave files behind for content that was never created.
            for (String key : storedKeys) {
                deleteQuietly(key, e);
            }
            throw e;
        }
    }

    /** Best-effort cleanup of an uploaded file after a later step failed; failures are attached to the original error. */
    private void deleteQuietly(String key, Exception original) {
        try {
            storageService.deleteFile(key);
        } catch (Exception cleanupFailure) {
            original.addSuppressed(cleanupFailure);
        }
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
    public ResponseEntity<Void> deleteContent(
            @PathVariable Long id,
            @RequestParam(defaultValue = "false", required = false) boolean hardDelete)
            throws Exception {

        if (hardDelete)
        {
            contentService.hardDeleteContent(id);
        }
        else {
            contentService.softDeleteContent(id);
        }

        return ResponseEntity.noContent().build();
    }
}
