package com.anjosflow.schedule;

import jakarta.validation.constraints.*;
import java.time.LocalTime;

public record AvailabilitySlot(
    @NotNull @Min(1) @Max(7) Integer dayOfWeek,
    @NotNull LocalTime startTime,
    @NotNull LocalTime endTime) {}
