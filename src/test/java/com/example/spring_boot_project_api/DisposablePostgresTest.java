package com.example.spring_boot_project_api;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

/** Database-backed tests use only the dedicated local test database and its test role. */
public abstract class DisposablePostgresTest {

    @DynamicPropertySource
    static void configureDisposableDatabase(DynamicPropertyRegistry registry) {
        String url = System.getenv("TOURISM_LINK_TEST_DB");
        if (url == null || !url.matches("jdbc:postgresql://(?:127[.]0[.]0[.]1|localhost):[0-9]{1,5}/tourism_link_test")) {
            throw new IllegalArgumentException(
                    "TOURISM_LINK_TEST_DB must point to a disposable loopback database named tourism_link_test");
        }

        registry.add("spring.datasource.url", () -> url);
        registry.add("spring.datasource.hikari.jdbc-url", () -> url);
        registry.add("spring.datasource.username", () -> "tourism_link_test");
        registry.add("spring.datasource.password", () -> "tourism_link_test");
        registry.add("spring.datasource.hikari.username", () -> "tourism_link_test");
        registry.add("spring.datasource.hikari.password", () -> "tourism_link_test");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "update");
        registry.add("app.seed-demo-data", () -> false);
        registry.add("auth.dev-password-reset-enabled", () -> false);
    }
}
