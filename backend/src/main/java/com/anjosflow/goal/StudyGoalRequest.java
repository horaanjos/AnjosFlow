package com.anjosflow.goal;

import jakarta.validation.constraints.*;
import java.time.*;

public record StudyGoalRequest(
    @NotNull GoalType type,
    @NotNull @Positive @Max(1000000) Integer targetValue,
    @NotNull LocalDate periodStart,
    @NotNull LocalDate periodEnd,
    @Positive Long subjectId) {}
