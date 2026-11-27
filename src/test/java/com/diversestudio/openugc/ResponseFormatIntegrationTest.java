package com.diversestudio.openugc;

import com.diversestudio.openugc.support.AbstractIntegrationTest;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.util.UUID;

import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Every success is JSON or an empty 204, and every error is ProblemDetail JSON,
 * including the rate limits, "guests disabled" and the errors from Spring Security
 * (401 for a missing/invalid token, 403 for a role that isn't allowed).
 */
class ResponseFormatIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    static String username;
    static String token;

    private static RequestPostProcessor fromIp(String ip) {
        return request -> {
            request.setRemoteAddr(ip);
            return request;
        };
    }

    private static String credentials(String user, String password) {
        return """
                {"username":"%s","credential":"%s"}
                """.formatted(user, password);
    }

    /** The body is RFC 9457 ProblemDetail with the given status. */
    private static void expectProblem(ResultActions result, int status) throws Exception {
        result.andExpect(status().is(status))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(status))
                .andExpect(jsonPath("$.detail", notNullValue()))
                .andExpect(jsonPath("$.timestamp", notNullValue()));
    }

    @BeforeEach
    void registerAndLoginOnce() throws Exception {
        if (token != null) {
            return;
        }
        String name = "fmt_" + UUID.randomUUID().toString().substring(0, 8);

        mockMvc.perform(post("/api/v1/auth/register").with(fromIp("192.0.2.100"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(credentials(name, "s3cret-pass")))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.userId", notNullValue()))
                .andExpect(jsonPath("$.username").value(name))
                .andExpect(jsonPath("$.role").value("USER"))
                .andExpect(jsonPath("$.credential").doesNotExist());

        String login = mockMvc.perform(post("/api/v1/auth/login").with(fromIp("192.0.2.100"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(credentials(name, "s3cret-pass")))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        token = JsonPath.read(login, "$.token");
        username = name;
    }

    // --- Success bodies ---

    @Test
    void patchUser_returnsTheUpdatedUser() throws Exception {
        mockMvc.perform(patch("/api/v1/users")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"aboutMe":"I make racing tracks"}
                                """))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.username").value(username))
                .andExpect(jsonPath("$.aboutMe").value("I make racing tracks"));
    }

    @Test
    void changePassword_isNoContent_andTheNewPasswordWorks() throws Exception {
        // Own account, so the shared one keeps its password for the other tests.
        String name = "pw_" + UUID.randomUUID().toString().substring(0, 8);
        RequestPostProcessor ip = fromIp("192.0.2.101");
        mockMvc.perform(post("/api/v1/auth/register").with(ip)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(credentials(name, "old-pass-123")))
                .andExpect(status().isCreated());
        String login = mockMvc.perform(post("/api/v1/auth/login").with(ip)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(credentials(name, "old-pass-123")))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String ownToken = JsonPath.read(login, "$.token");

        mockMvc.perform(patch("/api/v1/users/password")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + ownToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"currentPassword":"old-pass-123","newPassword":"new-pass-456"}
                                """))
                .andExpect(status().isNoContent())
                .andExpect(content().string(emptyString()));

        mockMvc.perform(post("/api/v1/auth/login").with(ip)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(credentials(name, "new-pass-456")))
                .andExpect(status().isOk());
    }

    // --- Security errors ---

    @Test
    void missingToken_isUnauthorizedProblem() throws Exception {
        expectProblem(mockMvc.perform(get("/api/v1/content")), 401);
    }

    @Test
    void invalidToken_isUnauthorizedProblem() throws Exception {
        expectProblem(mockMvc.perform(get("/api/v1/content")
                .header(HttpHeaders.AUTHORIZATION, "Bearer not-a-real-token")), 401);
    }

    @Test
    void userCallingAdminEndpoint_isForbiddenProblem() throws Exception {
        expectProblem(mockMvc.perform(get("/api/v1/admin/{id}", 1)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)), 403);
    }

    // --- Rate limit ---

    @Test
    void rateLimit_isProblemWithRetryAfter() throws Exception {
        // Own IP: 5 logins per minute are allowed, the 6th is rejected.
        RequestPostProcessor ip = fromIp("192.0.2.102");
        for (int i = 0; i < 5; i++) {
            mockMvc.perform(post("/api/v1/auth/login").with(ip)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(credentials(username, "s3cret-pass")))
                    .andExpect(status().isOk());
        }
        ResultActions limited = mockMvc.perform(post("/api/v1/auth/login").with(ip)
                .contentType(MediaType.APPLICATION_JSON)
                .content(credentials(username, "s3cret-pass")));
        expectProblem(limited, 429);
        limited.andExpect(header().exists(HttpHeaders.RETRY_AFTER))
                .andExpect(jsonPath("$.retryAfterSeconds", greaterThanOrEqualTo(1)));
    }

    // --- Guests disabled ---

    @Nested
    @TestPropertySource(properties = "guest.enabled=false")
    class GuestsDisabled {

        @Autowired
        MockMvc mockMvc;

        @Test
        void guestLogin_isForbiddenProblem() throws Exception {
            expectProblem(mockMvc.perform(post("/api/v1/auth/guest").with(fromIp("192.0.2.103"))), 403);
        }
    }
}
