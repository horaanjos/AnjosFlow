package com.anjosflow.task;

import jakarta.validation.constraints.*;
import java.time.*;

public record TaskRequest(
    @NotBlank(message = "O título é obrigatório.") @Size(min = 2, max = 150) String title,
    @Size(max = 1000) String description,
    @NotNull @Positive Long subjectId,
    @NotNull TaskStatus status,
    @NotNull Priority priority,
    LocalDate dueDate,
    @Positive @Max(10080) Integer estimatedMinutes) {
  public TaskRequest {
    title = title == null ? null : title.strip();
  }
}
