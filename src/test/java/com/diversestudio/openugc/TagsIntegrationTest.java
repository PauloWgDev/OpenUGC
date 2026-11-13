package com.diversestudio.openugc;

import com.diversestudio.openugc.support.AbstractIntegrationTest;
import com.jayway.jsonpath.JsonPath;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tags are optional, always saved lowercase, and a repeated tag in one request is kept once.
 * Content without tags returns {@code "tags": []}, never {@code [null]}.
 * <p>
 * Tag names include a random marker so tags created by other test classes don't interfere.
 */
class TagsIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    static String marker;
    static String token;

    /** Own client IP so the per-IP auth rate limit isn't shared with other test classes. */
    private static RequestPostProcessor fromOwnIp() {
        return request -> {
            request.setRemoteAddr("192.0.2.70");
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
                {"username":"tags_%s","credential":"s3cret-pass"}
                """.formatted(marker);

        mockMvc.perform(post("/api/auth/register").with(fromOwnIp())
                        .contentType(MediaType.APPLICATION_JSON).content(credentials))
                .andExpect(status().isOk());
        String login = mockMvc.perform(post("/api/auth/login").with(fromOwnIp())
                        .contentType(MediaType.APPLICATION_JSON).content(credentials))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        token = JsonPath.read(login, "$.token");
    }

    /** Uploads content with the given metadata JSON and returns the response body. */
    private String upload(String contentJson) throws Exception {
        return mockMvc.perform(multipart("/api/content")
                        .file(new MockMultipartFile("content", "", MediaType.APPLICATION_JSON_VALUE,
                                contentJson.getBytes(StandardCharsets.UTF_8)))
                        .file(new MockMultipartFile("file", "level.zip", "application/zip",
                                "data".getBytes(StandardCharsets.UTF_8)))
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
    }

    private void expectTagsOnGet(Integer id, Matcher<?> matcher) throws Exception {
        mockMvc.perform(get("/api/content/{id}", id)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tags", matcher));
    }

    @Test
    void uploadWithoutTagsField_isAllowedAndReturnsEmptyTags() throws Exception {
        String body = upload("""
                {"name":"no tags %s","version":1}
                """.formatted(marker));

        Integer id = JsonPath.read(body, "$.contentId");
        expectTagsOnGet(id, empty());
    }

    @Test
    void uploadWithEmptyTags_returnsEmptyTags() throws Exception {
        String body = upload("""
                {"name":"empty tags %s","version":1,"tags":[]}
                """.formatted(marker));

        Integer id = JsonPath.read(body, "$.contentId");
        expectTagsOnGet(id, empty());
    }

    @Test
    void repeatedAndMixedCaseTags_areSavedOnceInLowercase() throws Exception {
        String racing = "racing-" + marker;
        String puzzle = "puzzle-" + marker;
        String body = upload("""
                {"name":"dupes %s","version":1,
                 "tags":["Racing-%s"," racing-%s ","RACING-%s","Puzzle-%s"]}
                """.formatted(marker, marker, marker, marker, marker));

        // Upload response and get-by-id agree: lowercase, once each, alphabetical.
        Integer id = JsonPath.read(body, "$.contentId");
        List<String> uploadTags = JsonPath.read(body, "$.tags");
        assertThat(uploadTags).containsExactly(puzzle, racing);
        expectTagsOnGet(id, contains(puzzle, racing));
    }

    @Test
    void sameTagWithDifferentCase_reusesOneTag_andFilterIgnoresCase() throws Exception {
        String shared = "shared-" + marker;
        upload("""
                {"name":"first %s","version":1,"tags":["Shared-%s"]}
                """.formatted(marker, marker));
        upload("""
                {"name":"second %s","version":1,"tags":["SHARED-%s"]}
                """.formatted(marker, marker));

        // Both items carry the same lowercase tag, and the filter finds them whatever the case.
        mockMvc.perform(get("/api/content")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .param("tags", "Shared-" + marker))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.content[0].tags", contains(shared)))
                .andExpect(jsonPath("$.content[1].tags", contains(shared)));
    }
}
