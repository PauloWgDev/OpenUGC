package com.diversestudio.openugc;

import com.diversestudio.openugc.support.AbstractIntegrationTest;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * {@code latestDownloadsCount} counts downloads made in the last 7 days, going by the
 * download's own date, not by when the content was created.
 * <p>
 * Dates are moved into the past with plain SQL, since the API always stamps "now".
 */
class LatestDownloadsIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    JdbcTemplate jdbc;

    static String marker;
    static String token;

    /** Own client IP so the per-IP auth rate limit isn't shared with other test classes. */
    private static RequestPostProcessor fromOwnIp() {
        return request -> {
            request.setRemoteAddr("192.0.2.80");
            return request;
        };
    }

    @BeforeEach
    void registerAndLoginOnce() throws Exception {
        if (token != null) {
            return;
        }
        marker = UUID.randomUUID().toString().substring(0, 8);
        String credentials = """
                {"username":"latest_%s","credential":"s3cret-pass"}
                """.formatted(marker);

        mockMvc.perform(post("/api/v1/auth/register").with(fromOwnIp())
                        .contentType(MediaType.APPLICATION_JSON).content(credentials))
                .andExpect(status().isOk());
        String login = mockMvc.perform(post("/api/v1/auth/login").with(fromOwnIp())
                        .contentType(MediaType.APPLICATION_JSON).content(credentials))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        token = JsonPath.read(login, "$.token");
    }

    /** Uploads a content item and downloads it once; returns its id. */
    private long uploadAndDownloadOnce(String name) throws Exception {
        String json = """
                {"name":"%s","version":1}
                """.formatted(name);
        String body = mockMvc.perform(multipart("/api/v1/content")
                        .file(new MockMultipartFile("content", "", MediaType.APPLICATION_JSON_VALUE,
                                json.getBytes(StandardCharsets.UTF_8)))
                        .file(new MockMultipartFile("file", "level.zip", "application/zip",
                                "data".getBytes(StandardCharsets.UTF_8)))
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        long id = ((Number) JsonPath.read(body, "$.contentId")).longValue();

        mockMvc.perform(get("/api/v1/content/download/{id}", id)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk());
        return id;
    }

    /** Checks the count on get-by-id, the list and the stats endpoint. */
    private void expectCounts(long id, String name, int total, int latest) throws Exception {
        String auth = "Bearer " + token;

        mockMvc.perform(get("/api/v1/content/{id}", id).header(HttpHeaders.AUTHORIZATION, auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.downloadsCount").value(total))
                .andExpect(jsonPath("$.latestDownloadsCount").value(latest));

        mockMvc.perform(get("/api/v1/content").header(HttpHeaders.AUTHORIZATION, auth).param("prompt", name))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].contentId").value(id))
                .andExpect(jsonPath("$.content[0].downloadsCount").value(total))
                .andExpect(jsonPath("$.content[0].latestDownloadsCount").value(latest));

        mockMvc.perform(get("/api/v1/stats/{id}", id).header(HttpHeaders.AUTHORIZATION, auth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.downloadsCount").value(total))
                .andExpect(jsonPath("$.latestDownloadsCount").value(latest));
    }

    @Test
    void recentDownloadOfOldContent_countsAsLatest() throws Exception {
        String name = "old content " + marker;
        long id = uploadAndDownloadOnce(name);

        // Content created a month ago, downloaded just now: the old query (by content date) returned 0.
        jdbc.update("UPDATE content_dates SET created_at = NOW() - INTERVAL '30 days' WHERE content_id = ?", id);

        expectCounts(id, name, 1, 1);
    }

    @Test
    void oldDownloadOfNewContent_doesNotCountAsLatest() throws Exception {
        String name = "new content " + marker;
        long id = uploadAndDownloadOnce(name);

        // Content created now, downloaded 8 days ago: the old query (by content date) returned 1.
        jdbc.update("UPDATE downloads SET date = NOW() - INTERVAL '8 days' WHERE content_id = ?", id);

        expectCounts(id, name, 1, 0);
    }
}
