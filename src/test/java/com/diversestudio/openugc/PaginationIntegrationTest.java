package com.diversestudio.openugc;

import com.diversestudio.openugc.support.AbstractIntegrationTest;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * {@code totalElements} and {@code totalPages} must count every matching row,
 * not just the rows on the current page.
 * <p>
 * Setup: three users (each registering and logging in from its own IP, to stay under the
 * per-IP auth rate limit), three content items uploaded by the first user, and one rating
 * from each user on the first content item. Everything is named with a random marker so
 * rows created by other test classes don't change the counts.
 */
class PaginationIntegrationTest extends AbstractIntegrationTest {

    private static final int USERS = 3;
    private static final int CONTENTS = 3;

    @Autowired
    MockMvc mockMvc;

    static String marker;
    static String[] tokens;
    static long ratedContentId;

    private static RequestPostProcessor fromIp(int i) {
        return request -> {
            request.setRemoteAddr("192.0.2." + (50 + i));
            return request;
        };
    }

    @BeforeEach
    void setUpOnce() throws Exception {
        if (tokens != null) {
            return;
        }
        marker = "pg" + UUID.randomUUID().toString().substring(0, 8);
        String[] newTokens = new String[USERS];

        for (int i = 0; i < USERS; i++) {
            String credentials = """
                    {"username":"%s_u%d","credential":"s3cret-pass"}
                    """.formatted(marker, i);
            mockMvc.perform(post("/api/auth/register").with(fromIp(i))
                            .contentType(MediaType.APPLICATION_JSON).content(credentials))
                    .andExpect(status().isOk());
            String login = mockMvc.perform(post("/api/auth/login").with(fromIp(i))
                            .contentType(MediaType.APPLICATION_JSON).content(credentials))
                    .andExpect(status().isOk())
                    .andReturn().getResponse().getContentAsString();
            newTokens[i] = JsonPath.read(login, "$.token");
        }

        long firstContentId = -1;
        for (int i = 0; i < CONTENTS; i++) {
            String json = """
                    {"name":"%s level %d","description":"pagination","version":1,"tags":[]}
                    """.formatted(marker, i);
            String body = mockMvc.perform(multipart("/api/content")
                            .file(new MockMultipartFile("content", "", MediaType.APPLICATION_JSON_VALUE,
                                    json.getBytes(StandardCharsets.UTF_8)))
                            .file(new MockMultipartFile("file", "level.zip", "application/zip",
                                    "data".getBytes(StandardCharsets.UTF_8)))
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + newTokens[0]))
                    .andExpect(status().isOk())
                    .andReturn().getResponse().getContentAsString();
            if (i == 0) {
                firstContentId = ((Number) JsonPath.read(body, "$.contentId")).longValue();
            }
        }

        for (int i = 0; i < USERS; i++) {
            mockMvc.perform(post("/api/rating/{id}", firstContentId)
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + newTokens[i])
                            .param("rating", String.valueOf(i + 3)))
                    .andExpect(status().isOk());
        }

        ratedContentId = firstContentId;
        tokens = newTokens;
    }

    private String auth() {
        return "Bearer " + tokens[0];
    }

    @Test
    void contentPage_totalCountsAllMatchingContent() throws Exception {
        mockMvc.perform(get("/api/content")
                        .header(HttpHeaders.AUTHORIZATION, auth())
                        .param("prompt", marker)
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.totalElements").value(CONTENTS))
                .andExpect(jsonPath("$.totalPages").value(2));
    }

    @Test
    void contentPage_lastPage_keepsTheSameTotal() throws Exception {
        mockMvc.perform(get("/api/content")
                        .header(HttpHeaders.AUTHORIZATION, auth())
                        .param("prompt", marker)
                        .param("size", "2")
                        .param("page", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.totalElements").value(CONTENTS));
    }

    @Test
    void userPage_totalCountsAllMatchingUsers() throws Exception {
        mockMvc.perform(get("/api/users")
                        .header(HttpHeaders.AUTHORIZATION, auth())
                        .param("prompt", marker)
                        .param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.totalElements").value(USERS))
                .andExpect(jsonPath("$.totalPages").value(USERS));
    }

    @Test
    void ratingPage_totalCountsAllRatings() throws Exception {
        mockMvc.perform(get("/api/rating/{id}", ratedContentId)
                        .header(HttpHeaders.AUTHORIZATION, auth())
                        .param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.totalElements").value(USERS));
    }

    @Test
    void ratingPage_withRatingFilter_countsOnlyThatValue() throws Exception {
        // Ratings are 3, 4 and 5, one each.
        mockMvc.perform(get("/api/rating/{id}", ratedContentId)
                        .header(HttpHeaders.AUTHORIZATION, auth())
                        .param("size", "1")
                        .param("rating", "4"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1));
    }
}
