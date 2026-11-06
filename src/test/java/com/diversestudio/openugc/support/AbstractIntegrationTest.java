package com.diversestudio.openugc.support;

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

/**
 * Base class for integration tests. Boots the full Spring context against a real
 * PostgreSQL instance running in a Testcontainers-managed Docker container.
 * <p>
 * The container is a singleton: started once in a static initializer and reused by
 * every test class that extends this one, so the suite doesn't pay to restart Postgres
 * per class. Testcontainers' Ryuk sidecar stops it when the JVM exits.
 * <p>
 * Subclasses inherit {@code @SpringBootTest} + {@code @AutoConfigureMockMvc}, so they
 * only need to {@code @Autowired MockMvc} and write their requests.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("postgres")
public abstract class AbstractIntegrationTest {

    static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>("postgres:17")
                    .withDatabaseName("openugc_test")
                    .withUsername("test")
                    .withPassword("test")
                    .withInitScript("db/init-test.sql");

    static {
        POSTGRES.start();
    }

    @DynamicPropertySource
    static void datasourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }
}
