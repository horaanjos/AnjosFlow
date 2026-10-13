package com.anjosflow.schedule;

import java.time.*;
import java.util.List;

public record PlanProposal(String fingerprint, List<Block> blocks, List<Unscheduled> unscheduled) {
  public record Block(Long taskId, Long subjectId, String title, Instant startAt, Instant endAt) {}

  public record Unscheduled(Long taskId, String title, int remainingMinutes) {}
}
