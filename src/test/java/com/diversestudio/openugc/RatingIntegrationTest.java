package com.diversestudio.openugc;

import com.diversestudio.openugc.support.AbstractIntegrationTest;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * {@code POST /api/v1/rating/{id}} takes a JSON body {@code {"rating": 1-5, "comment": "..."}}.
 * Out-of-range or missing ratings and too-long comments are rejected with 400 (before the content
 * lookup), and a missing content is 404, not 500.
 */
class RatingIntegrationTest extends AbstractIntegrationTest {

    /** No content is ever created with this id in the test database. */
    private static final long MISSING_CONTENT_ID = 999_999_999L;

    @Autowired
    MockMvc mockMvc;

    static String token;
    static long contentId;

    /** Own client IP so the per-IP auth rate limit isn't shared with other test classes. */
    private static RequestPostProcessor fromOwnIp() {
        return request -> {
            request.setRemoteAddr("192.0.2.30");
            return request;
        };
    }

    @BeforeEach
    void registerAndLoginOnce() throws Exception {
        if (token != null) {
            return;
        }
        String marker = UUID.randomUUID().toString().substring(0, 8);
        String credentials = """
                {"username":"rating_%s","credential":"s3cret-pass"}
                """.formatted(marker);

        mockMvc.perform(post("/api/v1/auth/register")
                        .with(fromOwnIp())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(credentials))
                .andExpect(status().isCreated());

        MvcResult login = mockMvc.perform(post("/api/v1/auth/login")
                        .with(fromOwnIp())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(credentials))
                .andExpect(status().isOk())
                .andReturn();
        String newToken = JsonPath.read(login.getResponse().getContentAsString(), "$.token");

        String json = """
                {"name":"rating %s","description":"rating body","version":1,"tags":[]}
                """.formatted(marker);
        String body = mockMvc.perform(multipart("/api/v1/content")
                        .file(new MockMultipartFile("content", "", MediaType.APPLICATION_JSON_VALUE,
                                json.getBytes(StandardCharsets.UTF_8)))
                        .file(new MockMultipartFile("file", "level.zip", "application/zip",
                                "data".getBytes(StandardCharsets.UTF_8)))
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + newToken))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        contentId = ((Number) JsonPath.read(body, "$.contentId")).longValue();
        token = newToken;
    }

    private ResultActions rate(long id, String jsonBody) throws Exception {
        return mockMvc.perform(post("/api/v1/rating/{id}", id)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonBody));
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "6", "-1", "1000"})
    void outOfRangeRating_isRejected(String rating) throws Exception {
        // Validated before the content lookup, so a missing id still gives 400 here.
        rate(MISSING_CONTENT_ID, "{\"rating\":" + rating + "}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.rating").exists());
    }

    @Test
    void missingRating_isRejected() throws Exception {
        rate(contentId, "{\"comment\":\"no score\"}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.rating").exists());
    }

    @Test
    void nonNumericRating_isRejected() throws Exception {
        rate(contentId, "{\"rating\":\"five\"}")
                .andExpect(status().isBadRequest());
    }

    @Test
    void tooLongComment_isRejected() throws Exception {
        String comment = "a".repeat(2_001);
        rate(contentId, "{\"rating\":4,\"comment\":\"" + comment + "\"}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.comment").exists());
    }

    @Test
    void commentInBody_isSaved_andBlankCommentIsNull() throws Exception {
        rate(contentId, "{\"rating\":4,\"comment\":\"Great map, with spaces & symbols!\"}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rating").value(4.0))
                .andExpect(jsonPath("$.comment").value("Great map, with spaces & symbols!"));

        rate(contentId, "{\"rating\":5,\"comment\":\"   \"}")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.comment").doesNotExist());
    }

    @Test
    void validRatingOnMissingContent_isNotFound() throws Exception {
        rate(MISSING_CONTENT_ID, "{\"rating\":3}")
                .andExpect(status().isNotFound());
    }
}
