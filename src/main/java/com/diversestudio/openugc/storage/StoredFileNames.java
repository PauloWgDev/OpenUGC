package com.diversestudio.openugc.storage;

import java.util.Locale;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Naming rules for uploaded files.
 * <p>
 * The name a client sends with an upload is never used as a storage path. Every file is
 * stored under a server-generated key ({@code <uuid>} or {@code <uuid>.<ext>}), so two
 * uploads can never collide and a crafted name ({@code ../../x}, {@code /etc/x},
 * {@code C:\x}) can never point outside the storage location. The original name is only
 * used to keep its extension, and only if that extension is plain lowercase alphanumerics.
 * <p>
 * The name a user sees when downloading is built from the content's name plus the stored
 * extension, see {@link #downloadName(String, String)}.
 */
public final class StoredFileNames {

    private static final Pattern SAFE_EXTENSION = Pattern.compile("[a-z0-9]{1,16}");

    /** Characters removed from download names: control chars and ones that are unsafe in headers or on Windows. */
    private static final Pattern UNSAFE_NAME_CHARS = Pattern.compile("[\\p{Cntrl}\"*:<>?|]");

    private static final int MAX_DOWNLOAD_BASE_LENGTH = 100;

    static final String FALLBACK_DOWNLOAD_NAME = "download";

    private StoredFileNames() {
    }

    /**
     * Generates a fresh storage key, keeping the original file's extension when it is safe.
     *
     * @param originalFilename the name sent by the client; may be {@code null}
     * @return a key such as {@code 3f1c...-9a2b.zip}, or just the UUID if there is no safe extension
     */
    public static String newKey(String originalFilename) {
        return newKeyWithExtension(extensionOf(originalFilename));
    }

    /**
     * Generates a fresh storage key with the given extension.
     *
     * @param extension an already-validated extension without the dot, or empty/{@code null} for none
     */
    public static String newKeyWithExtension(String extension) {
        String id = UUID.randomUUID().toString();
        return (extension == null || extension.isEmpty()) ? id : id + "." + extension;
    }

    /**
     * Returns the lowercase extension of a file name or stored value, without the dot,
     * or an empty string if there is none or it is not plain {@code [a-z0-9]{1,16}}.
     * Any directory or URL path in front of the name is ignored. Dot-files such as
     * {@code .htaccess} have no extension.
     */
    public static String extensionOf(String filename) {
        if (filename == null) {
            return "";
        }
        String base = lastPathSegment(filename);
        int dot = base.lastIndexOf('.');
        if (dot <= 0 || dot == base.length() - 1) {
            return "";
        }
        String extension = base.substring(dot + 1).toLowerCase(Locale.ROOT);
        return SAFE_EXTENSION.matcher(extension).matches() ? extension : "";
    }

    /**
     * Builds the file name offered to the user on download: the content's name, made safe,
     * plus the extension of the stored file. The extension is not repeated if the content
     * name already ends with it ({@code "level.zip"} stays {@code "level.zip"}).
     *
     * @param contentName the content's display name; may be {@code null}
     * @param storedValue the stored key (or a legacy stored URL) of the file being downloaded
     */
    public static String downloadName(String contentName, String storedValue) {
        String base = sanitizeDownloadBase(contentName);
        String extension = extensionOf(storedValue);
        if (extension.isEmpty() || base.toLowerCase(Locale.ROOT).endsWith("." + extension)) {
            return base;
        }
        return base + "." + extension;
    }

    /**
     * Turns a value read from the database into a storage key.
     * <p>
     * Rows written before keys were introduced stored a full public URL (local storage) or the
     * bare original file name (cloud storage). Stripping the configured base URL handles both,
     * so old rows keep working without a data migration.
     */
    static String keyFromStoredValue(String storedValue, String baseUrl) {
        if (storedValue == null || storedValue.isBlank()) {
            throw new IllegalArgumentException("Stored file reference is empty");
        }
        String key = (baseUrl != null && !baseUrl.isEmpty() && storedValue.startsWith(baseUrl))
                ? storedValue.substring(baseUrl.length())
                : storedValue;
        if (key.isBlank()) {
            throw new IllegalArgumentException("Stored file reference is empty");
        }
        return key;
    }

    static String sanitizeDownloadBase(String name) {
        if (name == null) {
            return FALLBACK_DOWNLOAD_NAME;
        }
        String cleaned = UNSAFE_NAME_CHARS.matcher(name).replaceAll("")
                .replace('/', '_')
                .replace('\\', '_')
                .strip();
        // No leading dots: the result is never hidden, ".", or "..".
        cleaned = cleaned.replaceFirst("^\\.+", "").strip();
        if (cleaned.codePointCount(0, cleaned.length()) > MAX_DOWNLOAD_BASE_LENGTH) {
            cleaned = cleaned.substring(0, cleaned.offsetByCodePoints(0, MAX_DOWNLOAD_BASE_LENGTH)).strip();
        }
        return cleaned.isEmpty() ? FALLBACK_DOWNLOAD_NAME : cleaned;
    }

    private static String lastPathSegment(String name) {
        int cut = Math.max(name.lastIndexOf('/'), name.lastIndexOf('\\'));
        return cut >= 0 ? name.substring(cut + 1) : name;
    }
}
