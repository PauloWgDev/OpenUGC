package com.diversestudio.openugc.config;

/**
 * Base path of the public API. Every controller maps under it, so the version
 * lives in one place. Breaking changes go to a new version (e.g. /api/v2)
 * instead of changing this one.
 */
public final class ApiPaths {

    public static final String V1 = "/api/v1";

    private ApiPaths() {
    }
}
