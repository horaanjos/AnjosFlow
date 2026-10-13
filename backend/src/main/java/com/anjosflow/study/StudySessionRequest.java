package com.anjosflow.study;

import jakarta.validation.constraints.*;
import java.time.*;
import java.util.UUID;

public record StudySessionRequest(
    @NotNull @Positive Long subjectId,
    @Positive Long taskId,
    @NotNull Instant startedAt,
    @NotNull Instant endedAt,
    @Size(max = 1000) String notes,
    @NotNull UUID clientKey) {}
