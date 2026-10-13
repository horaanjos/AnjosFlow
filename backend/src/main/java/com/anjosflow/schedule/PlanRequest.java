package com.anjosflow.schedule;

import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.util.UUID;

public record PlanRequest(
    @NotNull LocalDate weekStart,
    @Min(1) @Max(180) int sessionMinutes,
    @Min(0) @Max(60) int breakMinutes,
    UUID clientKey,
    String fingerprint) {}
