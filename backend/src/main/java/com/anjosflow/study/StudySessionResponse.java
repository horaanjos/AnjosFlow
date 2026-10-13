package com.anjosflow.study;

import java.time.*;
import java.util.UUID;

public record StudySessionResponse(
    Long id,
    Long subjectId,
    Long taskId,
    Instant startedAt,
    Instant endedAt,
    String notes,
    UUID clientKey,
    Integer durationMinutes,
    Instant createdAt,
    Instant updatedAt) {
  public static StudySessionResponse from(StudySession entity) {
    return new StudySessionResponse(
        entity.getId(),
        entity.getSubjectId(),
        entity.getTaskId(),
        entity.getStartedAt(),
        entity.getEndedAt(),
        entity.getNotes(),
        entity.getClientKey(),
        entity.getDurationMinutes(),
        entity.getCreatedAt(),
        entity.getUpdatedAt());
  }
}
