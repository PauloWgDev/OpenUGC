package com.diversestudio.openugc;

import com.diversestudio.openugc.support.AbstractIntegrationTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Checks how the client IP is resolved, using the per-IP rate limit on
 * {@code POST /api/auth/guest} (5 requests per minute) as the observable effect.
 * <p>
 * This needs a real Tomcat ({@code RANDOM_PORT}): {@code X-Forwarded-For} is handled by Tomcat's
 * {@code RemoteIpValve} ({@code server.forward-headers-strategy=native}), which MockMvc bypasses.
 * Test requests come from 127.0.0.1, which is a trusted proxy by default.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ClientIpIntegrationTest extends AbstractIntegrationTest {

    private static final int GUEST_LIMIT = 5;

    @Autowired
    TestRestTemplate rest;

    /** Calls the guest endpoint once, claiming to be {@code forwardedFor}. */
    static HttpStatus guestLoginAs(TestRestTemplate rest, String forwardedFor) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Forwarded-For", forwardedFor);
        ResponseEntity<String> response = rest.exchange(
                "/api/auth/guest", HttpMethod.POST, new HttpEntity<>(headers), String.class);
        return HttpStatus.valueOf(response.getStatusCode().value());
    }

    @Test
    void trustedProxy_eachForwardedClientGetsItsOwnLimit() {
        // Behind a trusted proxy, different clients must not share one bucket.
        // Before the fix, getRemoteAddr() was the proxy for everyone, so request 6 got 429.
        for (int i = 1; i <= GUEST_LIMIT + 1; i++) {
            assertThat(guestLoginAs(rest, "203.0.113." + i)).isEqualTo(HttpStatus.OK);
        }
    }

    /** No trusted proxies: X-Forwarded-For comes from the client and must be ignored. */
    @Nested
    @TestPropertySource(properties = "server.tomcat.remoteip.internal-proxies=")
    class UntrustedPeer {

        @Autowired
        TestRestTemplate rest;

        @Test
        void spoofedForwardedFor_doesNotBypassRateLimit() {
            // A different fake IP on every request must still hit the limit of the real address.
            for (int i = 1; i <= GUEST_LIMIT; i++) {
                assertThat(guestLoginAs(rest, "198.51.100." + i)).isEqualTo(HttpStatus.OK);
            }
            assertThat(guestLoginAs(rest, "198.51.100.99")).isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
        }
    }
}
