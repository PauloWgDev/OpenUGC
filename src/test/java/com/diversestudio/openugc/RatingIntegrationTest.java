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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * {@code POST /api/rating/{id}} must reject values outside 1-5 (they would skew the average)
 * and answer 404, not 500, when the content does not exist.
 */
class RatingIntegrationTest extends AbstractIntegrationTest {

    /** No content is ever created with this id in the test database. */
    private static final long MISSING_CONTENT_ID = 999_999_999L;

    @Autowired
    MockMvc mockMvc;

    static String token;

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
        String credentials = """
                {"username":"rating_%s","credential":"s3cret-pass"}
                """.formatted(UUID.randomUUID().toString().substring(0, 8));

        mockMvc.perform(post("/api/auth/register")
                        .with(fromOwnIp())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(credentials))
                .andExpect(status().isOk());

        MvcResult login = mockMvc.perform(post("/api/auth/login")
                        .with(fromOwnIp())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(credentials))
                .andExpect(status().isOk())
                .andReturn();
        token = JsonPath.read(login.getResponse().getContentAsString(), "$.token");
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "6", "-1", "1000"})
    void outOfRangeRating_isRejected(String rating) throws Exception {
        // Checked before the content lookup, so a missing id still gives 400 here.
        mockMvc.perform(post("/api/rating/{id}", MISSING_CONTENT_ID)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .param("rating", rating))
                .andExpect(status().isBadRequest());
    }

    @Test
    void validRatingOnMissingContent_isNotFound() throws Exception {
        mockMvc.perform(post("/api/rating/{id}", MISSING_CONTENT_ID)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .param("rating", "3"))
                .andExpect(status().isNotFound());
    }
}
