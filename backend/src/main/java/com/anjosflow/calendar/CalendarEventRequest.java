package com.anjosflow.calendar;

import jakarta.validation.constraints.*;
import java.time.*;

public record CalendarEventRequest(
    @NotBlank(message = "O título é obrigatório.") @Size(min = 2, max = 150) String title,
    @Size(max = 1000) String description,
    @Positive Long subjectId,
    @NotNull Instant startAt,
    @NotNull Instant endAt,
    @NotNull EventType type) {
  public CalendarEventRequest {
    title = title == null ? null : title.strip();
  }
}
