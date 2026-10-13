package com.anjosflow.common.health;

import java.time.Instant;
import com.fasterxml.jackson.annotation.JsonIgnore;

public record HealthResponse(String status, String database, Instant timestamp) {
    @JsonIgnore
    public boolean isHealthy() {
        return "UP".equals(status);
    }
}
