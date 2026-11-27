package com.diversestudio.openugc.config;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/** How {@code cors.allowed-origins} turns into CORS rules. */
class CorsConfigTest {

    private static CorsConfiguration configFor(List<String> origins) {
        CorsProperties properties = new CorsProperties();
        properties.setAllowedOrigins(origins);
        // JwtUtil isn't used to build the CORS rules.
        CorsConfigurationSource source = new SecurityConfig(null, properties, null).corsConfigurationSource();
        return source.getCorsConfiguration(new MockHttpServletRequest("GET", "/api/v1/content"));
    }

    @Test
    void defaultAllowsEveryOrigin_withoutCredentials() {
        CorsConfiguration config = configFor(new CorsProperties().getAllowedOrigins());

        assertThat(config).isNotNull();
        assertThat(config.checkOrigin("https://anything.example")).isNotNull();
        assertThat(config.getAllowCredentials()).isFalse();
        assertThat(config.getExposedHeaders()).contains("Content-Disposition");
    }

    @Test
    void listedOriginsAndPatterns_areAllowed_othersAreNot() {
        CorsConfiguration config = configFor(List.of("https://mygame.com", " https://*.itch.zone "));

        assertThat(config.checkOrigin("https://mygame.com")).isEqualTo("https://mygame.com");
        assertThat(config.checkOrigin("https://html-classic.itch.zone")).isEqualTo("https://html-classic.itch.zone");
        assertThat(config.checkOrigin("https://evil.example")).isNull();
    }

    @Test
    void emptyList_turnsCorsOff() {
        assertThat(configFor(List.of())).isNull();
        assertThat(configFor(List.of(" "))).isNull();
    }
}
