package com.diversestudio.openugc;

import com.diversestudio.openugc.support.AbstractIntegrationTest;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Error cases must answer with the right status and a ProblemDetail body:
 * failed login 401, taken username 409, missing content or stats 404.
 */
class ErrorStatusIntegrationTest extends AbstractIntegrationTest {

    /** No content is ever created with this id in the test database. */
    private static final long MISSING_CONTENT_ID = 999_999_999L;

    @Autowired
    MockMvc mockMvc;

    static String username;
    static String token;

    /** Own client IP so the per-IP auth rate limit isn't shared with other test classes. */
    private static RequestPostProcessor fromOwnIp() {
        return request -> {
            request.setRemoteAddr("192.0.2.40");
            return request;
        };
    }

    private static String credentials(String user, String password) {
        return """
                {"username":"%s","credential":"%s"}
                """.formatted(user, password);
    }

    @BeforeEach
    void registerAndLoginOnce() throws Exception {
        if (token != null) {
            return;
        }
        username = "errors_" + UUID.randomUUID().toString().substring(0, 8);

        mockMvc.perform(post("/api/auth/register")
                        .with(fromOwnIp())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(credentials(username, "s3cret-pass")))
                .andExpect(status().isOk());

        MvcResult login = mockMvc.perform(post("/api/auth/login")
                        .with(fromOwnIp())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(credentials(username, "s3cret-pass")))
                .andExpect(status().isOk())
                .andReturn();
        token = JsonPath.read(login.getResponse().getContentAsString(), "$.token");
    }

    @Test
    void loginWithWrongPassword_isUnauthorized() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .with(fromOwnIp())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(credentials(username, "wrong-pass")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void registerTakenUsername_isConflict() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .with(fromOwnIp())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(credentials(username, "another-pass")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void getMissingContent_isNotFound() throws Exception {
        mockMvc.perform(get("/api/content/{id}", MISSING_CONTENT_ID)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void downloadMissingContent_isNotFound() throws Exception {
        mockMvc.perform(get("/api/content/download/{id}", MISSING_CONTENT_ID)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void getStatsOfMissingContent_isNotFound() throws Exception {
        mockMvc.perform(get("/api/stats/{id}", MISSING_CONTENT_ID)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }
}
