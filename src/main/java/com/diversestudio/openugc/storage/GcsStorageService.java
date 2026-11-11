package com.diversestudio.openugc.storage;


import com.diversestudio.openugc.exception.ResourceNotFoundException;
import com.google.cloud.storage.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
@ConditionalOnProperty(name = "file.storage.type", havingValue = "cloud")
public class GcsStorageService implements StorageService
{
    private final Storage storage;
    private final String bucketName;
    private final String fileBaseUrl;

    public GcsStorageService(@Value("${file.cloud.bucket}") String bucketName,
                             @Value("${file.cloud.baseUrl}") String fileBaseUrl) {
        this.storage = StorageOptions.getDefaultInstance().getService();
        this.bucketName = bucketName;
        this.fileBaseUrl = fileBaseUrl;
    }

    @Override
    public void uploadFile(String key, MultipartFile file, String contentType) throws IOException {
        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException("Storage key is empty");
        }

        BlobInfo blobInfo = BlobInfo.newBuilder(BlobId.of(bucketName, key))
                .setContentType(contentType) // decided by the server, never the client's claim
                .build();

        // doesNotExist(): the write fails instead of overwriting an existing object.
        storage.create(blobInfo, file.getBytes(), Storage.BlobTargetOption.doesNotExist());
    }

    @Override
    public Resource loadFileAsResource(String storedValue) {
        String key = StoredFileNames.keyFromStoredValue(storedValue, fileBaseUrl);
        Blob blob = storage.get(bucketName, key);

        if (blob == null || !blob.exists()) {
            throw new ResourceNotFoundException("File not found");
        }

        // Return as in-memory Resource
        return new ByteArrayResource(blob.getContent());
    }

    @Override
    public void deleteFile(String storedValue) {
        storage.delete(bucketName, StoredFileNames.keyFromStoredValue(storedValue, fileBaseUrl));
    }

    @Override
    public String getPublicUrl(String storedValue) {
        if (storedValue == null) {
            return null;
        }
        return fileBaseUrl + StoredFileNames.keyFromStoredValue(storedValue, fileBaseUrl);
    }
}
