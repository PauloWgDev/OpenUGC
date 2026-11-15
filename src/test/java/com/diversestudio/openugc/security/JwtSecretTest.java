package com.diversestudio.openugc.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** The app must refuse to start with a missing, short, placeholder or public example JWT secret. */
class JwtSecretTest {

    private static final String GOOD_SECRET = "k3Jx9vQ2mT8rW5yZ1aB4cD7eF0gH6iJ9lM2nP5qR8sU=";

    @Test
    void blankSecret_isRejected() {
        assertThatThrownBy(() -> new JwtUtil(""))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not set");
        assertThatThrownBy(() -> new JwtUtil(null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void placeholderFromExampleConfigs_isRejected() {
        assertThatThrownBy(() -> new JwtUtil("CHANGE_ME"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("at least 32 bytes")
                .hasMessageContaining("openssl rand -base64 48");
    }

    @Test
    void publicExampleSecret_isRejectedEvenThoughItIsLongEnough() {
        assertThatThrownBy(() -> new JwtUtil(JwtUtil.PUBLIC_EXAMPLE_SECRET))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("public example value");
        assertThatThrownBy(() -> new JwtUtil(" " + JwtUtil.PUBLIC_EXAMPLE_SECRET + " "))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void secretOf32BytesOrMore_isAccepted_andTokensRoundTrip() {
        JwtUtil jwt = new JwtUtil(GOOD_SECRET);

        String token = jwt.generateToken(42L, "alice", List.of("USER"));

        assertThat(jwt.extractAuthorities(token))
                .extracting(GrantedAuthority::getAuthority)
                .containsExactly("ROLE_USER");
    }
}
