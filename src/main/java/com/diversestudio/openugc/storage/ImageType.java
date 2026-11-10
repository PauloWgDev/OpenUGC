package com.diversestudio.openugc.storage;

import java.util.Optional;

/**
 * Image formats accepted for thumbnails, detected from the file's first bytes.
 * <p>
 * The client's file name and Content-Type are not trusted: a file is a PNG only if it
 * starts like one. SVG is deliberately not supported, because it can carry scripts.
 */
public enum ImageType {
    PNG("png", "image/png"),
    JPEG("jpg", "image/jpeg"),
    WEBP("webp", "image/webp");

    /** Number of leading bytes {@link #detect(byte[])} needs to recognise every type. */
    public static final int HEADER_LENGTH = 12;

    private final String extension;
    private final String contentType;

    ImageType(String extension, String contentType) {
        this.extension = extension;
        this.contentType = contentType;
    }

    public String extension() {
        return extension;
    }

    public String contentType() {
        return contentType;
    }

    /**
     * Detects the image type from the start of a file.
     *
     * @param header the first bytes of the file (up to {@link #HEADER_LENGTH}; fewer if the file is shorter)
     */
    public static Optional<ImageType> detect(byte[] header) {
        if (startsWith(header, 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A)) {
            return Optional.of(PNG);
        }
        if (startsWith(header, 0xFF, 0xD8, 0xFF)) {
            return Optional.of(JPEG);
        }
        // RIFF....WEBP
        if (startsWith(header, 'R', 'I', 'F', 'F') && header.length >= 12
                && header[8] == 'W' && header[9] == 'E' && header[10] == 'B' && header[11] == 'P') {
            return Optional.of(WEBP);
        }
        return Optional.empty();
    }

    private static boolean startsWith(byte[] data, int... prefix) {
        if (data.length < prefix.length) {
            return false;
        }
        for (int i = 0; i < prefix.length; i++) {
            if ((data[i] & 0xFF) != prefix[i]) {
                return false;
            }
        }
        return true;
    }
}
