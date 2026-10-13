package com.anjosflow.common.health;

import java.time.Instant;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class HealthService {
    private final JdbcTemplate jdbcTemplate;

    public HealthService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public HealthResponse check() {
        try {
            Integer result = jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            boolean available = Integer.valueOf(1).equals(result);
            return new HealthResponse(available ? "UP" : "DOWN", available ? "UP" : "DOWN", Instant.now());
        } catch (DataAccessException exception) {
            // A resposta pública não contém credenciais nem detalhes da conexão.
            return new HealthResponse("DOWN", "DOWN", Instant.now());
        }
    }
}
