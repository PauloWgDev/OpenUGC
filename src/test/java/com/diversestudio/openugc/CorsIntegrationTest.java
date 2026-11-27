package com.diversestudio.openugc;

import com.diversestudio.openugc.support.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Browser clients on another origin (e.g. a Unity WebGL build) can call the API:
 * preflight requests succeed without a token, and responses carry the CORS headers,
 * error responses included.
 */
class CorsIntegrationTest extends AbstractIntegrationTest {

    private static final String ORIGIN = "https://mygame.example";

    @Autowired
    MockMvc mockMvc;

    @Test
    void preflight_toAuthenticatedEndpoint_succeedsWithoutToken() throws Exception {
        mockMvc.perform(options("/api/v1/content")
                        .header(HttpHeaders.ORIGIN, ORIGIN)
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "authorization, content-type"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, ORIGIN))
                .andExpect(header().exists(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS))
                .andExpect(header().exists(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS))
                .andExpect(header().doesNotExist(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS));
    }

    @Test
    void errorResponses_carryCorsHeaders_soTheClientCanReadThem() throws Exception {
        mockMvc.perform(get("/api/v1/content/thumbnail/999999999")
                        .header(HttpHeaders.ORIGIN, ORIGIN))
                .andExpect(status().isNotFound())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, ORIGIN));
    }

    @Test
    void sameOriginOrNonBrowserRequests_getNoCorsHeaders() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(header().doesNotExist(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
    }
}
