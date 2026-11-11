package com.diversestudio.openugc;

import com.diversestudio.openugc.support.AbstractIntegrationTest;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Uploads go through the real endpoint and local storage: client file names must never
 * decide where a file is written, and downloads must get a safe, readable name.
 */
class ContentUploadIntegrationTest extends AbstractIntegrationTest {

    private static final String KEY_RE =
            "[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}(\\.[a-z0-9]{1,16})?";

    private static final byte[] PNG = {(byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A, 0, 0, 0, 0x0D};

    @Autowired
    MockMvc mockMvc;

    @Value("${file.storage.location}")
    String storageLocation;

    @Value("${file.storage.baseUrl}")
    String baseUrl;

    static String token;

    @BeforeEach
    void registerAndLoginOnce() throws Exception {
        if (token != null) {
            return;
        }
        String credentials = """
                {"username":"upload_%s","credential":"s3cret-pass"}
                """.formatted(UUID.randomUUID().toString().substring(0, 8));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(credentials))
                .andExpect(status().isOk());

        MvcResult login = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(credentials))
                .andExpect(status().isOk())
                .andReturn();
        token = JsonPath.read(login.getResponse().getContentAsString(), "$.token");
    }

    private MockMultipartHttpServletRequestBuilder upload(MockMultipartFile file, MockMultipartFile thumbnail) {
        String json = """
                {"name":"My Level","description":"test","version":1,"tags":[]}
                """;
        MockMultipartHttpServletRequestBuilder request = multipart("/api/content")
                .file(new MockMultipartFile("content", "", MediaType.APPLICATION_JSON_VALUE,
                        json.getBytes(StandardCharsets.UTF_8)))
                .file(file);
        if (thumbnail != null) {
            request.file(thumbnail);
        }
        request.header(HttpHeaders.AUTHORIZATION, "Bearer " + token);
        return request;
    }

    private static MockMultipartFile mainFile(String originalName, String content) {
        return new MockMultipartFile("file", originalName, "application/zip", content.getBytes(StandardCharsets.UTF_8));
    }

    private String uploadAndGetBody(MockMultipartFile file, MockMultipartFile thumbnail) throws Exception {
        return mockMvc.perform(upload(file, thumbnail))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
    }

    private Path storageRoot() {
        return Paths.get(storageLocation).toAbsolutePath().normalize();
    }

    private long storedFileCount() throws Exception {
        try (Stream<Path> files = Files.list(storageRoot())) {
            return files.count();
        }
    }

    @Test
    void twoUploadsWithTheSameName_doNotOverwriteEachOther() throws Exception {
        String first = uploadAndGetBody(mainFile("same.zip", "first"), null);
        String second = uploadAndGetBody(mainFile("same.zip", "second"), null);

        String firstUrl = JsonPath.read(first, "$.data");
        String secondUrl = JsonPath.read(second, "$.data");
        assertThat(firstUrl).isNotEqualTo(secondUrl);
        assertThat(firstUrl.substring(baseUrl.length())).matches(KEY_RE).endsWith(".zip");

        Integer firstId = JsonPath.read(first, "$.contentId");
        Integer secondId = JsonPath.read(second, "$.contentId");

        MvcResult download = mockMvc.perform(get("/api/content/download/" + firstId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn();
        assertThat(download.getResponse().getContentAsString()).isEqualTo("first");
        ContentDisposition disposition =
                ContentDisposition.parse(download.getResponse().getHeader(HttpHeaders.CONTENT_DISPOSITION));
        assertThat(disposition.isAttachment()).isTrue();
        assertThat(disposition.getFilename()).isEqualTo("My Level.zip");

        String secondContent = mockMvc.perform(get("/api/content/download/" + secondId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        assertThat(secondContent).isEqualTo("second");
    }

    @Test
    void pathTraversalName_isStoredInsideStorageUnderGeneratedKey() throws Exception {
        String body = uploadAndGetBody(mainFile("../../evil.zip", "payload"), null);

        String url = JsonPath.read(body, "$.data");
        String key = url.substring(baseUrl.length());
        assertThat(key).matches(KEY_RE);
        assertThat(storageRoot().resolve(key)).hasContent("payload");
        assertThat(storageRoot().getParent().resolve("evil.zip")).doesNotExist();
    }

    @Test
    void imageThumbnail_isStoredWithItsRealType() throws Exception {
        MockMultipartFile thumbnail = new MockMultipartFile("thumbnail", "evil.svg", "image/svg+xml", PNG);

        String body = uploadAndGetBody(mainFile("level.zip", "data"), thumbnail);

        String thumbnailUrl = JsonPath.read(body, "$.thumbnail");
        assertThat(thumbnailUrl).startsWith(baseUrl).endsWith(".png");
    }

    @Test
    void nonImageThumbnail_isRejectedAndNothingIsStored() throws Exception {
        long before = storedFileCount();
        MockMultipartFile thumbnail = new MockMultipartFile("thumbnail", "thumb.png", "image/png",
                "<html><script>alert(1)</script></html>".getBytes(StandardCharsets.UTF_8));

        mockMvc.perform(upload(mainFile("level.zip", "data"), thumbnail))
                .andExpect(status().isBadRequest());

        assertThat(storedFileCount()).isEqualTo(before);
    }

    @Test
    void emptyMainFile_isRejected() throws Exception {
        mockMvc.perform(upload(new MockMultipartFile("file", "empty.zip", "application/zip", new byte[0]), null))
                .andExpect(status().isBadRequest());
    }
}
