package com.diversestudio.openugc.storage;

import com.diversestudio.openugc.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
@ConditionalOnProperty(name = "file.storage.type", havingValue = "local", matchIfMissing = true)
public class LocalStorageService implements StorageService {

    private final Path fileStorageLocation;
    private final String fileBaseUrl;

    public LocalStorageService(@Value("${file.storage.location}") String storageLocation,
                               @Value("${file.storage.baseUrl}") String fileBaseUrl) throws IOException {
        this.fileStorageLocation = Paths.get(storageLocation).toAbsolutePath().normalize();
        this.fileBaseUrl = fileBaseUrl;
        Files.createDirectories(this.fileStorageLocation);
    }

    @Override
    public void uploadFile(String key, MultipartFile file, String contentType) throws IOException {
        Path target = resolveInsideStorage(key);
        try (InputStream in = file.getInputStream()) {
            // No REPLACE_EXISTING: an existing file is never overwritten.
            Files.copy(in, target);
        }
    }

    @Override
    public Resource loadFileAsResource(String storedValue) {
        Path filePath = resolveInsideStorage(StoredFileNames.keyFromStoredValue(storedValue, fileBaseUrl));
        if (!Files.isRegularFile(filePath) || !Files.isReadable(filePath)) {
            throw new ResourceNotFoundException("File not found");
        }
        return new FileSystemResource(filePath);
    }

    @Override
    public void deleteFile(String storedValue) throws IOException {
        Path filePath = resolveInsideStorage(StoredFileNames.keyFromStoredValue(storedValue, fileBaseUrl));
        Files.deleteIfExists(filePath);
    }

    @Override
    public String getPublicUrl(String storedValue) {
        if (storedValue == null) {
            return null;
        }
        return fileBaseUrl + StoredFileNames.keyFromStoredValue(storedValue, fileBaseUrl);
    }

    /**
     * Resolves a key to a path directly inside the storage directory, rejecting anything that
     * would land elsewhere (absolute paths, {@code ..}, sub-directories, invalid characters).
     */
    private Path resolveInsideStorage(String key) {
        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException("Storage key is empty");
        }
        Path resolved;
        try {
            resolved = fileStorageLocation.resolve(key).normalize();
        } catch (InvalidPathException e) {
            throw new IllegalArgumentException("Invalid storage key", e);
        }
        if (!fileStorageLocation.equals(resolved.getParent())) {
            throw new IllegalArgumentException("Storage key resolves outside the storage directory");
        }
        return resolved;
    }
}
