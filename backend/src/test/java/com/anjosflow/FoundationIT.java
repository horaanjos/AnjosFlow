package com.anjosflow;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import com.anjosflow.common.health.HealthResponse;
import static org.assertj.core.api.Assertions.assertThat;

// Executado apenas por mvn verify -Pintegration; exige PostgreSQL real disponível.
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class FoundationIT {
    @Autowired private TestRestTemplate http;
    @Autowired private JdbcTemplate jdbc;

    @Test
    void startsApplicationMigratesDatabaseAndRespondsToHealth() {
        var response = http.getForEntity("/api/health", HealthResponse.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().status()).isEqualTo("UP");
        assertThat(response.getBody().database()).isEqualTo("UP");
        assertThat(jdbc.queryForObject("SELECT name FROM application_metadata WHERE id = 1", String.class))
                .isEqualTo("AnjosFlow");
        assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM flyway_schema_history WHERE version = '1' AND success = true", Integer.class))
                .isEqualTo(1);
    }
}
