package com.diversestudio.openugc.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;


@ConfigurationProperties(prefix = "guest")
@Getter
@Setter
public class GuestProperties {
    private boolean enabled;
    private boolean canPost;
}
