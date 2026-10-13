package com.anjosflow.calendar;

import java.time.Instant;

public record CalendarItem(
    String id,
    String title,
    String description,
    Long subjectId,
    Instant startAt,
    Instant endAt,
    EventType type,
    String source,
    Long sourceId) {}
