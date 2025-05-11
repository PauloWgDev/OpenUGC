package com.diversestudio.unityapi.storage;


import com.google.cloud.storage.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.net.URL;
import java.util.concurrent.TimeUnit;

@Service
@ConditionalOnProperty(name = "file.storage.type", havingValue = "cloud")
public class GcsStorageService implements StorageService
{
    private final Storage storage;
    private final String bucketName;

    @Value("${file.cloud.baseUrl}")
    private String fileBaseUrl;

    public GcsStorageService(@Value("${file.cloud.bucket}") String bucketName) {
        this.storage = StorageOptions.getDefaultInstance().getService();
        this.bucketName = bucketName;
    }

    @Override
    public String uploadFile(MultipartFile file) throws Exception {
        String fileName = file.getOriginalFilename();
        if (fileName.contains("..")) {
            throw new Exception("Invalid file name: " + fileName);
        }

        BlobId blobId = BlobId.of(bucketName, fileName);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                .setContentType(file.getContentType())
                .build();

        storage.create(blobInfo, file.getBytes());

        return fileName;
    }

    @Override
    public Resource loadFileAsResource(String fileName) throws Exception {
        Blob blob = storage.get(bucketName, fileName);

        if (blob == null || !blob.exists()) {
            throw new Exception("File not found in GCS: " + fileName);
        }

        // Read content into a byte array
        byte[] content = blob.getContent();

        // Return as in-memory Resource
        return new org.springframework.core.io.ByteArrayResource(content) {
            @Override
            public String getFilename() {
                return fileName;
            }
        };
    }

    @Override
    public void deleteFile(String fileUrl) throws Exception {
        if (!fileUrl.startsWith(fileBaseUrl)) {
            throw new Exception("Invalid URL format.");
        }
        String fileName = fileUrl.substring(fileBaseUrl.length());
        storage.delete(bucketName, fileName);
    }
}
