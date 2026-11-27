package com.diversestudio.openugc.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Limits for the paginated lists (content, users, ratings), from {@code pagination.*}.
 */
@ConfigurationProperties(prefix = "pagination")
@Getter
@Setter
public class PaginationProperties {

    /** Largest {@code size} a client may ask for. Bigger values return 400. */
    private int maxPageSize = 100;
}
