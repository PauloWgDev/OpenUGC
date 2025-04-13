package com.diversestudio.unityapi.storage;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

/**
 * Common interface for file storage services.
 * This interface defines the basic operation(s) for file storage. Implementations of this
 * interface can differ by storing files locally (e.g., on the server's file system) or remotely
 * (e.g., in a cloud storage service like Google Cloud Storage).
 */
public interface StorageService {

    /**
     * Uploads a file to the storage service and returns a publicly accessible URL for the file.
     *
     * @param file the file to be uploaded
     * @return the public URL to access the uploaded file
     * @throws Exception if an error occurs during file upload
     */
    String uploadFile(MultipartFile file) throws Exception;

    /**
     * Loads a file as a Resource.
     *
     * @param fileName the name of the file to retrieve
     * @return the Resource corresponding to the file
     * @throws Exception if the file cannot be found or is not readable
     */
    Resource loadFileAsResource(String fileName) throws Exception;

    /**
     * Deletes a file from the storage service given its public URL.
     *
     * @param fileUrl the public URL of the file to delete
     * @throws Exception if deletion fails
     */
    void deleteFile(String fileUrl) throws Exception;
}
