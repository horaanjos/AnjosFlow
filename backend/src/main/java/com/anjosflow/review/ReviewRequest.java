package com.anjosflow.review;

import jakarta.validation.constraints.*;
import java.time.*;

public record ReviewRequest(
    @NotNull @Positive Long subjectId,
    @NotBlank(message = "O título é obrigatório.") @Size(min = 2, max = 150) String title,
    @Size(max = 1000) String notes) {
  public ReviewRequest {
    title = title == null ? null : title.strip();
  }
}
