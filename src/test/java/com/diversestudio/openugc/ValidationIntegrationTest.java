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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Invalid request bodies must be rejected with 400 and an {@code errors} map naming the field,
 * instead of reaching the database (500) or being saved.
 */
class ValidationIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    static String token;

    /** Own client IP so the per-IP auth rate limit isn't shared with other test classes. */
    private static RequestPostProcessor fromOwnIp() {
        return request -> {
            request.setRemoteAddr("192.0.2.60");
            return request;
        };
    }

    @BeforeEach
    void registerAndLoginOnce() throws Exception {
        if (token != null) {
            return;
        }
        String credentials = """
                {"username":"valid_%s","credential":"s3cret-pass"}
                """.formatted(UUID.randomUUID().toString().substring(0, 8));

        mockMvc.perform(post("/api/auth/register").with(fromOwnIp())
                        .contentType(MediaType.APPLICATION_JSON).content(credentials))
                .andExpect(status().isOk());
        String login = mockMvc.perform(post("/api/auth/login").with(fromOwnIp())
                        .contentType(MediaType.APPLICATION_JSON).content(credentials))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        token = JsonPath.read(login, "$.token");
    }

    @Test
    void register_blankUsername_isRejected() throws Exception {
        mockMvc.perform(post("/api/auth/register").with(fromOwnIp())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"   ","credential":"s3cret-pass"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.username").exists());
    }

    @Test
    void register_shortPassword_isRejected() throws Exception {
        mockMvc.perform(post("/api/auth/register").with(fromOwnIp())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"short_pw_user","credential":"abc"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.credential").exists());
    }

    @Test
    void patchUser_blankUsername_isRejected() throws Exception {
        mockMvc.perform(patch("/api/users")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"username":"     "}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.username").exists());
    }

    @Test
    void changePassword_blankNewPassword_isRejected() throws Exception {
        mockMvc.perform(patch("/api/users/password")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"currentPassword":"s3cret-pass","newPassword":""}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.newPassword").exists());
    }

    @Test
    void upload_missingName_isRejected() throws Exception {
        String json = """
                {"description":"no name","version":1,"tags":[]}
                """;
        mockMvc.perform(multipart("/api/content")
                        .file(new MockMultipartFile("content", "", MediaType.APPLICATION_JSON_VALUE,
                                json.getBytes(StandardCharsets.UTF_8)))
                        .file(new MockMultipartFile("file", "level.zip", "application/zip",
                                "data".getBytes(StandardCharsets.UTF_8)))
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.name").exists());
    }
}
