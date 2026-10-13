package com.anjosflow.review;

import java.time.*;

public record ReviewResponse(
    Long id,
    Long subjectId,
    String title,
    String notes,
    Instant lastReviewedAt,
    LocalDate nextReviewAt,
    Integer intervalDays,
    Integer reviewCount,
    Instant createdAt,
    Instant updatedAt) {
  public static ReviewResponse from(Review entity) {
    return new ReviewResponse(
        entity.getId(),
        entity.getSubjectId(),
        entity.getTitle(),
        entity.getNotes(),
        entity.getLastReviewedAt(),
        entity.getNextReviewAt(),
        entity.getIntervalDays(),
        entity.getReviewCount(),
        entity.getCreatedAt(),
        entity.getUpdatedAt());
  }
}
