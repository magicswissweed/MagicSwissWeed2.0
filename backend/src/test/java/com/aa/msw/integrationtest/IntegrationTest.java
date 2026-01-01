package com.aa.msw.integrationtest;

import com.aa.msw.helper.PublicSpotListConfiguration;
import io.restassured.RestAssured;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.test.annotation.DirtiesContext;

import static io.restassured.RestAssured.given;

/**
 * Base class for integration tests.
 * Configured to prevent deadlocks by disabling background tasks and ensuring
 * context isolation between test classes.
 */
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "spring.flyway.clean-disabled=false",
                "spring.jpa.hibernate.ddl-auto=none",
                "spring.task.scheduling.enabled=false" // Prevents background tasks from holding locks
        }
)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS) // Closes all connections after class finishes
public abstract class IntegrationTest {

    @LocalServerPort
    public int port;

    @Autowired
    public PublicSpotListConfiguration publicSpotListConfiguration;

    @BeforeEach
    public void setupRestAssured() {
        RestAssured.port = port;
    }

    /**
     * This method now only handles data seeding.
     * Schema cleaning/migration is handled by the MigrationStrategy below during startup.
     */
    @BeforeAll
    public void seedData() {
        publicSpotListConfiguration.persistPublicSpots();
    }

    /**
     * Configuration to ensure Flyway cleans and migrates as part of the Spring startup lifecycle.
     * This is significantly more robust than calling clean() manually in @BeforeAll.
     */
    @Configuration
    static class FlywayTestConfiguration {
        @Bean
        public FlywayMigrationStrategy cleanMigrationStrategy() {
            return flyway -> {
                flyway.clean();
                flyway.migrate();
            };
        }
    }

    protected RequestSpecification getTemplateRequest(TestUser user) {
        return given()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + user.token())
                .port(port);
    }
}
