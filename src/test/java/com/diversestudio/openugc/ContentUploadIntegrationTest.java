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
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
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

    /** MockMvc requests go to http://localhost, so that's the base of the URLs in responses. */
    private static final String API_BASE = "http://localhost/api/content/";

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

    private Set<String> storedFileNames() throws Exception {
        try (Stream<Path> files = Files.list(storageRoot())) {
            return files.map(path -> path.getFileName().toString()).collect(Collectors.toSet());
        }
    }

    /** Names of the files that appeared in storage since {@code before} was taken. */
    private Set<String> newStoredFiles(Set<String> before) throws Exception {
        Set<String> after = new HashSet<>(storedFileNames());
        after.removeAll(before);
        return after;
    }

    @Test
    void twoUploadsWithTheSameName_doNotOverwriteEachOther() throws Exception {
        String first = uploadAndGetBody(mainFile("same.zip", "first"), null);
        String second = uploadAndGetBody(mainFile("same.zip", "second"), null);

        Integer firstId = JsonPath.read(first, "$.contentId");
        Integer secondId = JsonPath.read(second, "$.contentId");

        // data links to the counted, authenticated download endpoint, never to storage.
        assertThat((String) JsonPath.read(first, "$.data")).isEqualTo(API_BASE + "download/" + firstId);
        assertThat((String) JsonPath.read(second, "$.data")).isEqualTo(API_BASE + "download/" + secondId);

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
        Set<String> before = storedFileNames();

        uploadAndGetBody(mainFile("../../evil.zip", "payload"), null);

        Set<String> added = newStoredFiles(before);
        assertThat(added).hasSize(1);
        String key = added.iterator().next();
        assertThat(key).matches(KEY_RE).endsWith(".zip");
        assertThat(storageRoot().resolve(key)).hasContent("payload");
        assertThat(storageRoot().getParent().resolve("evil.zip")).doesNotExist();
    }

    @Test
    void imageThumbnail_isStoredWithItsRealType() throws Exception {
        MockMultipartFile thumbnail = new MockMultipartFile("thumbnail", "evil.svg", "image/svg+xml", PNG);

        Set<String> before = storedFileNames();

        String body = uploadAndGetBody(mainFile("level.zip", "data"), thumbnail);

        Integer id = JsonPath.read(body, "$.contentId");
        assertThat((String) JsonPath.read(body, "$.thumbnail")).isEqualTo(API_BASE + "thumbnail/" + id);
        // Stored with the detected type's extension, not the client's ".svg".
        assertThat(newStoredFiles(before)).anySatisfy(key -> assertThat(key).matches(KEY_RE).endsWith(".png"));
    }

    @Test
    void thumbnail_isPublicInlineCachedAndNotCounted() throws Exception {
        MockMultipartFile thumbnail = new MockMultipartFile("thumbnail", "thumb.png", "image/png", PNG);
        String body = uploadAndGetBody(mainFile("level.zip", "data"), thumbnail);
        Integer id = JsonPath.read(body, "$.contentId");

        // No Authorization header.
        MvcResult result = mockMvc.perform(get("/api/content/thumbnail/" + id))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, "image/png"))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "inline"))
                .andReturn();
        assertThat(result.getResponse().getContentAsByteArray()).isEqualTo(PNG);
        assertThat(result.getResponse().getHeader(HttpHeaders.CACHE_CONTROL))
                .contains("max-age=86400").contains("public");

        // Fetching the thumbnail is not a download.
        mockMvc.perform(get("/api/content/" + id)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.downloadsCount").value(0));
    }

    @Test
    void thumbnail_contentWithoutThumbnail_isNotFound() throws Exception {
        String body = uploadAndGetBody(mainFile("level.zip", "data"), null);
        Integer id = JsonPath.read(body, "$.contentId");

        assertThat((String) JsonPath.read(body, "$.thumbnail")).isNull();
        mockMvc.perform(get("/api/content/thumbnail/" + id))
                .andExpect(status().isNotFound());
    }

    @Test
    void thumbnail_missingContent_isNotFound() throws Exception {
        mockMvc.perform(get("/api/content/thumbnail/999999999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void download_stillRequiresToken() throws Exception {
        String body = uploadAndGetBody(mainFile("level.zip", "data"), null);
        Integer id = JsonPath.read(body, "$.contentId");

        MvcResult result = mockMvc.perform(get("/api/content/download/" + id)).andReturn();
        assertThat(result.getResponse().getStatus()).isIn(401, 403);
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
