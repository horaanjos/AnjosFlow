package com.anjosflow.goal;

import java.time.*;

public record StudyGoalResponse(
    Long id,
    GoalType type,
    Integer targetValue,
    LocalDate periodStart,
    LocalDate periodEnd,
    Long subjectId,
    Instant createdAt,
    Instant updatedAt) {
  public static StudyGoalResponse from(StudyGoal entity) {
    return new StudyGoalResponse(
        entity.getId(),
        entity.getType(),
        entity.getTargetValue(),
        entity.getPeriodStart(),
        entity.getPeriodEnd(),
        entity.getSubjectId(),
        entity.getCreatedAt(),
        entity.getUpdatedAt());
  }
}
