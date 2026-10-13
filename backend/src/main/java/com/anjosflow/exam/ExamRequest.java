package com.anjosflow.exam;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.*;

public record ExamRequest(
    @NotBlank(message = "O título é obrigatório.") @Size(min = 2, max = 150) String title,
    @Size(max = 1000) String description,
    @Positive Long subjectId,
    @NotNull Instant scheduledAt,
    @Size(max = 200) String location,
    @NotNull ExamStatus status,
    @DecimalMin("0") @Digits(integer = 6, fraction = 2) BigDecimal score,
    @DecimalMin(value = "0", inclusive = false) @Digits(integer = 6, fraction = 2)
        BigDecimal maxScore) {
  public ExamRequest {
    title = title == null ? null : title.strip();
  }
}
