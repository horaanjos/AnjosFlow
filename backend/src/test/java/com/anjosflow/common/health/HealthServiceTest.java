package com.anjosflow.common.health;

import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.jdbc.core.JdbcTemplate;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class HealthServiceTest {
    private final JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
    private final HealthService service = new HealthService(jdbcTemplate);

    @Test
    void returnsUpWhenDatabaseResponds() {
        when(jdbcTemplate.queryForObject("SELECT 1", Integer.class)).thenReturn(1);
        HealthResponse result = service.check();
        assertThat(result.status()).isEqualTo("UP");
        assertThat(result.database()).isEqualTo("UP");
        assertThat(result.timestamp()).isNotNull();
    }

    @Test
    void returnsDownWithoutExposingConnectionDetails() {
        when(jdbcTemplate.queryForObject("SELECT 1", Integer.class))
                .thenThrow(new DataAccessResourceFailureException("secret connection details"));
        HealthResponse result = service.check();
        assertThat(result.status()).isEqualTo("DOWN");
        assertThat(result.database()).isEqualTo("DOWN");
        assertThat(result.toString()).doesNotContain("secret");
    }

    @Test
    void unexpectedQueryResultDoesNotReportHealthy() {
        when(jdbcTemplate.queryForObject("SELECT 1", Integer.class)).thenReturn(null);
        assertThat(service.check().isHealthy()).isFalse();
    }
}
