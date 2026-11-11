package com.diversestudio.openugc.storage;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StoredFileNamesTest {

    private static final String UUID_RE = "[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}";

    @Test
    void newKey_isUuidPlusExtension() {
        assertThat(StoredFileNames.newKey("My Level.ZIP")).matches(UUID_RE + "\\.zip");
    }

    @Test
    void newKey_isUniqueForTheSameName() {
        assertThat(StoredFileNames.newKey("same.zip")).isNotEqualTo(StoredFileNames.newKey("same.zip"));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "../../etc/passwd",
            "/etc/passwd",
            "C:\\Windows\\system32\\evil.dll",
            "..\\..\\evil.zip",
            "a\"b\r\n.zip",
            ".htaccess",
            "no-extension",
            "trailing-dot.",
            "weird.ex/t",
            "long.abcdefghijklmnopq",
            "sp ace.z ip",
            "ünïcødé.zip"
    })
    void newKey_neverContainsAnythingFromThePathOrUnsafeCharacters(String original) {
        assertThat(StoredFileNames.newKey(original)).matches(UUID_RE + "(\\.[a-z0-9]{1,16})?");
    }

    @ParameterizedTest
    @NullAndEmptySource
    void newKey_handlesMissingName(String original) {
        assertThat(StoredFileNames.newKey(original)).matches(UUID_RE);
    }

    @ParameterizedTest
    @CsvSource({
            "level.zip, zip",
            "LEVEL.ZIP, zip",
            "archive.tar.gz, gz",
            "dir/sub/file.png, png",
            "C:\\x\\file.JPG, jpg",
            "My Map.unitypackage, unitypackage",
            "http://localhost:8080/storage/old name.unitypackage, unitypackage",
            "x.abcdefghijklmnopq, ''",
            "http://localhost:8080/storage/level.zip, zip",
            ".htaccess, ''",
            "noext, ''",
            "file., ''",
            "bad.e-x, ''"
    })
    void extensionOf(String name, String expected) {
        assertThat(StoredFileNames.extensionOf(name)).isEqualTo(expected);
    }

    @ParameterizedTest
    @CsvSource({
            "My Level, 3f1c.zip, My Level.zip",
            "My Level.zip, 3f1c.zip, My Level.zip",
            "AC/DC level, 3f1c.zip, AC_DC level.zip",
            "'  ..hidden  ', 3f1c.zip, hidden.zip",
            "Say \"hi\", 3f1c.zip, Say hi.zip",
            "My Level, 3f1c, My Level",
            "'', 3f1c.zip, download.zip",
            "..., 3f1c.zip, download.zip"
    })
    void downloadName(String contentName, String key, String expected) {
        assertThat(StoredFileNames.downloadName(contentName, key)).isEqualTo(expected);
    }

    @Test
    void downloadName_handlesNullAndControlCharacters() {
        assertThat(StoredFileNames.downloadName(null, "k.zip")).isEqualTo("download.zip");
        assertThat(StoredFileNames.downloadName("evil\r\nheader", "k.zip")).isEqualTo("evilheader.zip");
    }

    @Test
    void downloadName_isLengthCapped() {
        assertThat(StoredFileNames.downloadName("x".repeat(500), "k.zip")).hasSize(100 + ".zip".length());
    }

    @Test
    void keyFromStoredValue_stripsLegacyBaseUrl() {
        String base = "http://localhost:8080/storage/";
        assertThat(StoredFileNames.keyFromStoredValue(base + "level.zip", base)).isEqualTo("level.zip");
        assertThat(StoredFileNames.keyFromStoredValue("3f1c.zip", base)).isEqualTo("3f1c.zip");
    }

    @Test
    void keyFromStoredValue_rejectsEmpty() {
        String base = "http://localhost:8080/storage/";
        assertThatThrownBy(() -> StoredFileNames.keyFromStoredValue(base, base))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> StoredFileNames.keyFromStoredValue(" ", base))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
