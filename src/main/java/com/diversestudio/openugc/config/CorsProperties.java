package com.diversestudio.openugc.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * Which browser origins can call the API (CORS).
 * <p>
 * Set with {@code CORS_ALLOWED_ORIGINS}, a comma-separated list. Each entry is an origin
 * ({@code https://game.com}) or a pattern ({@code https://*.itch.zone}). {@code *} allows
 * every origin; an empty value turns CORS off, so only non-browser clients (native Unity
 * builds, servers) can call the API.
 */
@ConfigurationProperties(prefix = "cors")
@Getter
@Setter
public class CorsProperties {
    private List<String> allowedOrigins = new ArrayList<>(List.of("*"));
}
