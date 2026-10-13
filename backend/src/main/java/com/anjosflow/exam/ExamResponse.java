package com.anjosflow.exam;

import java.math.BigDecimal;
import java.time.*;

public record ExamResponse(
    Long id,
    String title,
    String description,
    Long subjectId,
    Instant scheduledAt,
    String location,
    ExamStatus status,
    BigDecimal score,
    BigDecimal maxScore,
    Instant createdAt,
    Instant updatedAt) {
  public static ExamResponse from(Exam entity) {
    return new ExamResponse(
        entity.getId(),
        entity.getTitle(),
        entity.getDescription(),
        entity.getSubjectId(),
        entity.getScheduledAt(),
        entity.getLocation(),
        entity.getStatus(),
        entity.getScore(),
        entity.getMaxScore(),
        entity.getCreatedAt(),
        entity.getUpdatedAt());
  }
}
