package com.diversestudio.unityapi.storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Service
@ConditionalOnProperty(name = "file.storage.type", havingValue = "local", matchIfMissing = true)
public class LocalStorageService implements StorageService {

    private final Path fileStorageLocation;

    // Inject the base URL defined in application.properties
    @Value("${file.storage.baseUrl}")
    private String fileBaseUrl;

    public LocalStorageService(@Value("${file.storage.location}") String storageLocation) throws Exception {
        this.fileStorageLocation = Paths.get(storageLocation).toAbsolutePath().normalize();
        Files.createDirectories(this.fileStorageLocation);
    }

    @Override
    public String uploadFile(MultipartFile file) throws Exception {
        String fileName = file.getOriginalFilename();

        //TODO: MAKE SURE THE SECURITY CANNOT BE BROKEN
        // Simple security check to prevent path traversal attacks.
        if(fileName.contains("..")){
            throw new Exception("Invalid file name: " + fileName);
        }

        // Determine target location and save the file.
        Path targetLocation = this.fileStorageLocation.resolve(fileName);
        Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

        // Return the public URL
        return fileBaseUrl + fileName;
    }

    @Override
    public Resource loadFileAsResource(String fileName) throws Exception {
        try {
            Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new Exception("File not found or not readable: " + fileName);
            }
        } catch (MalformedURLException ex) {
            throw new Exception("File not found: " + fileName, ex);
        }
    }

    @Override
    public void deleteFile(String fileUrl) throws Exception {
        // Ensure the fileUrl starts with the fileBaseUrl. Then extract the file name.
        if (!fileUrl.startsWith(fileBaseUrl)) {
            throw new Exception("File URL does not match the base URL.");
        }
        String fileName = fileUrl.substring(fileBaseUrl.length());
        Path targetLocation = this.fileStorageLocation.resolve(fileName).normalize();
        Files.deleteIfExists(targetLocation);
    }
}
