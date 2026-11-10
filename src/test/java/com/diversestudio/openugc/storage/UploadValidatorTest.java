package com.diversestudio.openugc.storage;

import com.diversestudio.openugc.exception.InvalidRequestException;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UploadValidatorTest {

    static final byte[] PNG = {(byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A, 0, 0, 0, 0x0D};
    static final byte[] JPEG = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0, 0, 0x10};
    static final byte[] WEBP = {'R', 'I', 'F', 'F', 0x24, 0, 0, 0, 'W', 'E', 'B', 'P', 'V', 'P', '8'};

    @Test
    void requireNonEmpty_rejectsMissingAndEmpty() {
        assertThatThrownBy(() -> UploadValidator.requireNonEmpty(null, "file"))
                .isInstanceOf(InvalidRequestException.class);
        assertThatThrownBy(() -> UploadValidator.requireNonEmpty(
                new MockMultipartFile("file", "x.zip", null, new byte[0]), "file"))
                .isInstanceOf(InvalidRequestException.class);
    }

    @Test
    void requireImage_detectsByContentNotByName() throws Exception {
        // Real PNG bytes with a misleading name and content type are still a PNG.
        assertThat(UploadValidator.requireImage(
                new MockMultipartFile("thumbnail", "evil.svg", "image/svg+xml", PNG), "thumbnail"))
                .isEqualTo(ImageType.PNG);
        assertThat(UploadValidator.requireImage(
                new MockMultipartFile("thumbnail", "a", null, JPEG), "thumbnail"))
                .isEqualTo(ImageType.JPEG);
        assertThat(UploadValidator.requireImage(
                new MockMultipartFile("thumbnail", "a", null, WEBP), "thumbnail"))
                .isEqualTo(ImageType.WEBP);
    }

    @Test
    void requireImage_rejectsNonImagesEvenWithImageNames() {
        byte[] html = "<html><script>alert(1)</script></html>".getBytes(StandardCharsets.UTF_8);
        byte[] svg = "<svg xmlns=\"http://www.w3.org/2000/svg\"/>".getBytes(StandardCharsets.UTF_8);

        assertThatThrownBy(() -> UploadValidator.requireImage(
                new MockMultipartFile("thumbnail", "thumb.png", "image/png", html), "thumbnail"))
                .isInstanceOf(InvalidRequestException.class);
        assertThatThrownBy(() -> UploadValidator.requireImage(
                new MockMultipartFile("thumbnail", "thumb.svg", "image/svg+xml", svg), "thumbnail"))
                .isInstanceOf(InvalidRequestException.class);
        assertThatThrownBy(() -> UploadValidator.requireImage(
                new MockMultipartFile("thumbnail", "t.png", "image/png", new byte[]{(byte) 0x89, 'P'}), "thumbnail"))
                .isInstanceOf(InvalidRequestException.class);
    }
}
