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

        // Optional: generate a signed URL (private access with expiration)
        URL signedUrl = storage.signUrl(blobInfo, 15, TimeUnit.MINUTES);
        return signedUrl.toString();

        // Or use public URL if the bucket/object is public:
        // return fileBaseUrl + fileName;
    }

    @Override
    public Resource loadFileAsResource(String fileName) throws Exception {
        String url = fileBaseUrl + fileName;
        return new UrlResource(url);
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
