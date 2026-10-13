package com.anjosflow.task;

import java.time.*;

public record TaskResponse(
    Long id,
    String title,
    String description,
    Long subjectId,
    TaskStatus status,
    Priority priority,
    LocalDate dueDate,
    Integer estimatedMinutes,
    Instant completedAt,
    Instant createdAt,
    Instant updatedAt) {
  public static TaskResponse from(Task entity) {
    return new TaskResponse(
        entity.getId(),
        entity.getTitle(),
        entity.getDescription(),
        entity.getSubjectId(),
        entity.getStatus(),
        entity.getPriority(),
        entity.getDueDate(),
        entity.getEstimatedMinutes(),
        entity.getCompletedAt(),
        entity.getCreatedAt(),
        entity.getUpdatedAt());
  }
}
