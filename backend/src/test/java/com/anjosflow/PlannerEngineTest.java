package com.anjosflow;

import static org.assertj.core.api.Assertions.*;

import com.anjosflow.schedule.*;
import com.anjosflow.task.Priority;
import java.time.*;
import java.util.*;
import org.junit.jupiter.api.Test;

class PlannerEngineTest {
  private final PlannerEngine engine = new PlannerEngine();
  private final Instant start = Instant.parse("2026-10-19T12:00:00Z");

  private PlannerEngine.Candidate task(Long id, int minutes, Priority priority, Instant deadline) {
    return new PlannerEngine.Candidate(id, 1L, "Tarefa " + id, minutes, priority, deadline);
  }

  @Test
  void respectsAvailabilityConflictsBreaksAndReportsUnscheduledWork() {
    var availability = List.of(new PlannerEngine.Window(start, start.plusSeconds(7200)));
    var busy = List.of(new PlannerEngine.Window(start.plusSeconds(1800), start.plusSeconds(3600)));
    var result =
        engine.generate(availability, busy, List.of(task(1L, 120, Priority.HIGH, null)), 25, 5);
    assertThat(result.blocks())
        .allSatisfy(
            b -> {
              assertThat(b.startAt()).isAfterOrEqualTo(start);
              assertThat(b.endAt()).isBeforeOrEqualTo(start.plusSeconds(7200));
              assertThat(
                      b.startAt().isBefore(busy.getFirst().endAt())
                          && b.endAt().isAfter(busy.getFirst().startAt()))
                  .isFalse();
            });
    assertThat(result.unscheduled()).hasSize(1);
    long allocated =
        result.blocks().stream()
            .mapToLong(b -> Duration.between(b.startAt(), b.endAt()).toMinutes())
            .sum();
    assertThat(allocated + result.unscheduled().getFirst().remainingMinutes()).isEqualTo(120);
  }

  @Test
  void prioritizesDeadlineThenPriorityAndIdDeterministically() {
    var candidates =
        List.of(
            task(3L, 25, Priority.HIGH, null),
            task(2L, 25, Priority.LOW, start),
            task(1L, 25, Priority.HIGH, start));
    var windows = List.of(new PlannerEngine.Window(start, start.plusSeconds(6000)));
    var first = engine.generate(windows, List.of(), candidates, 25, 5);
    assertThat(first.blocks()).extracting(PlanProposal.Block::taskId).containsExactly(1L, 2L, 3L);
    assertThat(engine.generate(windows, List.of(), candidates, 25, 5)).isEqualTo(first);
    assertThat(
            Duration.between(first.blocks().get(0).endAt(), first.blocks().get(1).startAt())
                .toMinutes())
        .isEqualTo(5);
  }

  @Test
  void noAvailabilityDoesNotScheduleAnything() {
    var result =
        engine.generate(List.of(), List.of(), List.of(task(1L, 25, Priority.LOW, null)), 25, 5);
    assertThat(result.blocks()).isEmpty();
    assertThat(result.unscheduled().getFirst().remainingMinutes()).isEqualTo(25);
  }

  @Test
  void eventCoveringWholeWindowLeavesNoBlocks() {
    var result =
        engine.generate(
            List.of(new PlannerEngine.Window(start, start.plusSeconds(1800))),
            List.of(new PlannerEngine.Window(start.minusSeconds(60), start.plusSeconds(2000))),
            List.of(task(1L, 25, Priority.LOW, null)),
            25,
            5);
    assertThat(result.blocks()).isEmpty();
  }

  @Test
  void breakIsRespectedAcrossAOneMinuteConflict() {
    var result =
        engine.generate(
            List.of(new PlannerEngine.Window(start, start.plusSeconds(7200))),
            List.of(new PlannerEngine.Window(start.plusSeconds(1500), start.plusSeconds(1560))),
            List.of(task(1L, 60, Priority.HIGH, null)),
            25,
            5);
    assertThat(result.blocks()).hasSizeGreaterThanOrEqualTo(2);
    assertThat(
            Duration.between(result.blocks().get(0).endAt(), result.blocks().get(1).startAt())
                .toMinutes())
        .isGreaterThanOrEqualTo(5);
  }
}
