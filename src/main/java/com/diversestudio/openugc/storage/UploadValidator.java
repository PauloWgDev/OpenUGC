package com.diversestudio.openugc.storage;

import com.diversestudio.openugc.exception.InvalidRequestException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

/**
 * Checks uploaded parts before anything is written to storage. Failures become HTTP 400
 * through {@link InvalidRequestException}.
 */
public final class UploadValidator {

    private UploadValidator() {
    }

    /** Rejects a missing or zero-byte part. */
    public static void requireNonEmpty(MultipartFile file, String partName) {
        if (file == null || file.isEmpty()) {
            throw new InvalidRequestException("'" + partName + "' must be a non-empty file");
        }
    }

    /**
     * Requires the part to be a PNG, JPEG or WebP image, judged by its content.
     *
     * @return the detected type, whose extension and content type should be used to store it
     */
    public static ImageType requireImage(MultipartFile file, String partName) throws IOException {
        requireNonEmpty(file, partName);
        byte[] header = new byte[ImageType.HEADER_LENGTH];
        int read;
        try (InputStream in = file.getInputStream()) {
            read = in.readNBytes(header, 0, header.length);
        }
        return ImageType.detect(Arrays.copyOf(header, read))
                .orElseThrow(() -> new InvalidRequestException(
                        "'" + partName + "' must be a PNG, JPEG or WebP image"));
    }
}
