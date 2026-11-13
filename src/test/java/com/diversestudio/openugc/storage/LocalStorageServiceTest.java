package com.diversestudio.openugc.storage;

import com.diversestudio.openugc.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LocalStorageServiceTest {

    static final String BASE_URL = "http://localhost/storage/";

    @TempDir
    Path tmp;

    Path root;
    LocalStorageService storage;

    @BeforeEach
    void setUp() throws Exception {
        root = tmp.resolve("storage");
        storage = new LocalStorageService(root.toString(), BASE_URL);
    }

    private static MockMultipartFile file(String content) {
        return new MockMultipartFile("file", "same.zip", "application/zip", content.getBytes());
    }

    @Test
    void storesUnderKeyAndLoadsBack() throws Exception {
        storage.uploadFile("k1.zip", file("hello"), "application/octet-stream");

        assertThat(root.resolve("k1.zip")).hasContent("hello");
        assertThat(storage.loadFileAsResource("k1.zip").getContentAsByteArray()).isEqualTo("hello".getBytes());
    }

    @Test
    void neverOverwritesAnExistingFile() throws Exception {
        storage.uploadFile("k1.zip", file("first"), "application/octet-stream");

        assertThatThrownBy(() -> storage.uploadFile("k1.zip", file("second"), "application/octet-stream"))
                .isInstanceOf(FileAlreadyExistsException.class);
        assertThat(root.resolve("k1.zip")).hasContent("first");
    }

    @ParameterizedTest
    @ValueSource(strings = {"../escape.zip", "../../escape.zip", "sub/x.zip", "./../x", ".", ".."})
    void rejectsKeysOutsideTheStorageDirectory(String key) {
        assertThatThrownBy(() -> storage.uploadFile(key, file("x"), "application/octet-stream"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> storage.loadFileAsResource(key))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> storage.deleteFile(key))
                .isInstanceOf(IllegalArgumentException.class);
        assertThat(tmp.resolve("escape.zip")).doesNotExist();
    }

    @Test
    void rejectsAbsoluteKeys() {
        String absolute = tmp.resolve("escape.zip").toAbsolutePath().toString();
        assertThatThrownBy(() -> storage.uploadFile(absolute, file("x"), "application/octet-stream"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThat(tmp.resolve("escape.zip")).doesNotExist();
    }

    @Test
    void missingFileIsNotFound() {
        assertThatThrownBy(() -> storage.loadFileAsResource("missing.zip"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void acceptsLegacyFullUrlValues() throws Exception {
        Files.createDirectories(root);
        Files.writeString(root.resolve("old.zip"), "legacy");

        assertThat(storage.loadFileAsResource(BASE_URL + "old.zip").getContentAsByteArray())
                .isEqualTo("legacy".getBytes());

        storage.deleteFile(BASE_URL + "old.zip");
        assertThat(root.resolve("old.zip")).doesNotExist();
    }

    @Test
    void worksWithoutBaseUrl() throws Exception {
        LocalStorageService noBaseUrl = new LocalStorageService(root.toString(), "");
        noBaseUrl.uploadFile("k2.zip", file("no base url"), "application/octet-stream");

        assertThat(noBaseUrl.loadFileAsResource("k2.zip").getContentAsByteArray())
                .isEqualTo("no base url".getBytes());
    }
}
