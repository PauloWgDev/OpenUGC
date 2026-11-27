package com.diversestudio.openugc.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI description served at /v3/api-docs and shown in Swagger UI (/swagger-ui.html).
 * <p>
 * Declares the JWT bearer scheme and requires it by default, so Swagger UI shows an
 * "Authorize" button: log in with POST /api/v1/auth/login (or /guest), paste the token,
 * and "Try it out" works on every endpoint. Postman also imports the collection with
 * bearer auth set up. Public endpoints opt out with an empty {@code @SecurityRequirements}.
 */
@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "OpenUGC API",
                version = "${openugc.version:dev}",
                description = "Open-source backend for user-generated content: accounts, uploads, "
                        + "search, downloads, ratings and stats. Errors use RFC 9457 ProblemDetail JSON.",
                license = @License(name = "Apache 2.0", url = "https://www.apache.org/licenses/LICENSE-2.0")
        ),
        security = @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH)
)
@SecurityScheme(
        name = OpenApiConfig.BEARER_AUTH,
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        description = "Token from POST /api/v1/auth/login or /api/v1/auth/guest (paste it without 'Bearer ')"
)
public class OpenApiConfig {

    public static final String BEARER_AUTH = "bearerAuth";
}
