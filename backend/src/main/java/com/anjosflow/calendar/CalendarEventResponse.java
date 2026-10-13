package com.anjosflow.calendar;

import java.time.*;

public record CalendarEventResponse(
    Long id,
    String title,
    String description,
    Long subjectId,
    Instant startAt,
    Instant endAt,
    EventType type,
    Instant createdAt,
    Instant updatedAt) {
  public static CalendarEventResponse from(CalendarEvent entity) {
    return new CalendarEventResponse(
        entity.getId(),
        entity.getTitle(),
        entity.getDescription(),
        entity.getSubjectId(),
        entity.getStartAt(),
        entity.getEndAt(),
        entity.getType(),
        entity.getCreatedAt(),
        entity.getUpdatedAt());
  }
}
