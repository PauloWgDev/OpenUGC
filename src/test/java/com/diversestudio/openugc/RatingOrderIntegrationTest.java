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

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * The ratings list has a stable order: newest first by default, {@code sort} on
 * createdAt/updatedAt/rating, 400 for other fields. Rating again keeps {@code createdAt}.
 * <p>
 * Setup: three users (own IPs, to stay under the per-IP auth rate limit), one content item,
 * and one rating per user, given in order: user 0 → 4, user 1 → 2, user 2 → 5.
 * Each class gets its own content, so ratings from other test classes don't interfere.
 */
class RatingOrderIntegrationTest extends AbstractIntegrationTest {

    private static final int USERS = 3;
    private static final int[] RATINGS = {4, 2, 5};

    @Autowired
    MockMvc mockMvc;

    static String[] tokens;
    static long[] userIds;
    static long contentId;

    private static RequestPostProcessor fromIp(int i) {
        return request -> {
            request.setRemoteAddr("192.0.2." + (90 + i));
            return request;
        };
    }

    @BeforeEach
    void setUpOnce() throws Exception {
        if (tokens != null) {
            return;
        }
        String marker = "ro" + UUID.randomUUID().toString().substring(0, 8);
        String[] newTokens = new String[USERS];
        long[] newUserIds = new long[USERS];

        for (int i = 0; i < USERS; i++) {
            String credentials = """
                    {"username":"%s_u%d","credential":"s3cret-pass"}
                    """.formatted(marker, i);
            mockMvc.perform(post("/api/v1/auth/register").with(fromIp(i))
                            .contentType(MediaType.APPLICATION_JSON).content(credentials))
                    .andExpect(status().isCreated());
            String login = mockMvc.perform(post("/api/v1/auth/login").with(fromIp(i))
                            .contentType(MediaType.APPLICATION_JSON).content(credentials))
                    .andExpect(status().isOk())
                    .andReturn().getResponse().getContentAsString();
            newTokens[i] = JsonPath.read(login, "$.token");
        }

        contentId = upload(newTokens[0], marker);

        for (int i = 0; i < USERS; i++) {
            String saved = rate(newTokens[i], contentId, RATINGS[i], null);
            newUserIds[i] = ((Number) JsonPath.read(saved, "$.userId")).longValue();
            // Keeps the createdAt values apart, so "newest first" has one right answer.
            Thread.sleep(20);
        }

        userIds = newUserIds;
        tokens = newTokens;
    }

    private long upload(String token, String marker) throws Exception {
        String json = """
                {"name":"%s level","description":"rating order","version":1,"tags":[]}
                """.formatted(marker);
        String body = mockMvc.perform(multipart("/api/v1/content")
                        .file(new MockMultipartFile("content", "", MediaType.APPLICATION_JSON_VALUE,
                                json.getBytes(StandardCharsets.UTF_8)))
                        .file(new MockMultipartFile("file", "level.zip", "application/zip",
                                "data".getBytes(StandardCharsets.UTF_8)))
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return ((Number) JsonPath.read(body, "$.contentId")).longValue();
    }

    private String rate(String token, long contentId, int rating, String comment) throws Exception {
        String body = comment == null
                ? "{\"rating\":%d}".formatted(rating)
                : "{\"rating\":%d,\"comment\":\"%s\"}".formatted(rating, comment);
        var request = post("/api/v1/rating/{id}", contentId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body);
        return mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.createdAt", notNullValue()))
                .andExpect(jsonPath("$.updatedAt", notNullValue()))
                .andReturn().getResponse().getContentAsString();
    }

    private String auth() {
        return "Bearer " + tokens[0];
    }

    @Test
    void defaultOrder_isNewestFirst() throws Exception {
        mockMvc.perform(get("/api/v1/rating/{id}", contentId).header(HttpHeaders.AUTHORIZATION, auth()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[*].userId",
                        contains((int) userIds[2], (int) userIds[1], (int) userIds[0])));
    }

    @Test
    void sortByRatingAsc_isLowestFirst() throws Exception {
        mockMvc.perform(get("/api/v1/rating/{id}", contentId)
                        .header(HttpHeaders.AUTHORIZATION, auth())
                        .param("sort", "rating,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[*].rating", contains(2.0, 4.0, 5.0)));
    }

    @Test
    void pages_neverRepeatOrSkipARating() throws Exception {
        long[] seen = new long[USERS];
        for (int page = 0; page < USERS; page++) {
            String body = mockMvc.perform(get("/api/v1/rating/{id}", contentId)
                            .header(HttpHeaders.AUTHORIZATION, auth())
                            .param("size", "1")
                            .param("page", String.valueOf(page)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(1)))
                    .andReturn().getResponse().getContentAsString();
            seen[page] = ((Number) JsonPath.read(body, "$.content[0].userId")).longValue();
        }
        assertThat(seen).containsExactlyInAnyOrder(userIds);
    }

    @Test
    void unknownSortField_isBadRequest() throws Exception {
        mockMvc.perform(get("/api/v1/rating/{id}", contentId)
                        .header(HttpHeaders.AUTHORIZATION, auth())
                        .param("sort", "comment,asc"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void ratingAgain_keepsCreatedAt_andUpdatesUpdatedAt() throws Exception {
        // Its own user and content, so this doesn't change the ratings the other tests check.
        String marker = "rr" + UUID.randomUUID().toString().substring(0, 8);
        RequestPostProcessor ip = request -> {
            request.setRemoteAddr("192.0.2.99");
            return request;
        };
        String credentials = """
                {"username":"%s","credential":"s3cret-pass"}
                """.formatted(marker);
        mockMvc.perform(post("/api/v1/auth/register").with(ip)
                        .contentType(MediaType.APPLICATION_JSON).content(credentials))
                .andExpect(status().isCreated());
        String login = mockMvc.perform(post("/api/v1/auth/login").with(ip)
                        .contentType(MediaType.APPLICATION_JSON).content(credentials))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String token = JsonPath.read(login, "$.token");
        long ownContentId = upload(token, marker);

        String first = rate(token, ownContentId, 3, "first try");
        Thread.sleep(20);
        String second = rate(token, ownContentId, 1, null);

        String createdFirst = JsonPath.read(first, "$.createdAt");
        String updatedFirst = JsonPath.read(first, "$.updatedAt");
        assertThat((String) JsonPath.read(second, "$.createdAt")).isEqualTo(createdFirst);
        assertThat((String) JsonPath.read(second, "$.updatedAt")).isNotEqualTo(updatedFirst);
        assertThat((Object) JsonPath.read(second, "$.comment")).isNull();

        // Still one rating, with the new value and the original date.
        mockMvc.perform(get("/api/v1/rating/{id}", ownContentId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].rating").value(1.0))
                .andExpect(jsonPath("$.content[0].createdAt").value(createdFirst));
    }
}
