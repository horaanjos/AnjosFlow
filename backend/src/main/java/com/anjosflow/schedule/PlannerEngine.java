package com.anjosflow.schedule;

import com.anjosflow.task.Priority;
import java.time.*;
import java.util.*;
import org.springframework.stereotype.Component;

@Component
public class PlannerEngine {
  public record Window(Instant startAt, Instant endAt) {}

  public record Candidate(
      Long id, Long subjectId, String title, int minutes, Priority priority, Instant deadline) {}

  public record Result(
      List<PlanProposal.Block> blocks, List<PlanProposal.Unscheduled> unscheduled) {}

  public Result generate(
      List<Window> availability,
      List<Window> busy,
      List<Candidate> input,
      int sessionMinutes,
      int breakMinutes) {
    var tasks =
        input.stream()
            .sorted(
                Comparator.comparing(
                        Candidate::deadline, Comparator.nullsLast(Comparator.naturalOrder()))
                    .thenComparing(
                        (Candidate c) -> c.priority().ordinal(), Comparator.reverseOrder())
                    .thenComparing(Candidate::id))
            .toList();
    Map<Long, Integer> remaining = new LinkedHashMap<>();
    tasks.forEach(t -> remaining.put(t.id(), t.minutes()));
    var blocks = new ArrayList<PlanProposal.Block>();
    Instant nextStart = Instant.MIN;
    for (var window :
        availability.stream().sorted(Comparator.comparing(Window::startAt)).toList()) {
      Instant cursor = window.startAt();
      List<Window> free = new ArrayList<>();
      for (var conflict :
          busy.stream()
              .filter(
                  b -> b.endAt().isAfter(window.startAt()) && b.startAt().isBefore(window.endAt()))
              .sorted(Comparator.comparing(Window::startAt))
              .toList()) {
        if (conflict.startAt().isAfter(cursor)) free.add(new Window(cursor, conflict.startAt()));
        if (conflict.endAt().isAfter(cursor)) cursor = conflict.endAt();
      }
      if (cursor.isBefore(window.endAt())) free.add(new Window(cursor, window.endAt()));
      for (var interval : free) {
        cursor = interval.startAt().isAfter(nextStart) ? interval.startAt() : nextStart;
        for (var task : tasks) {
          while (remaining.get(task.id()) > 0) {
            long available = Duration.between(cursor, interval.endAt()).toMinutes();
            if (available < 1) break;
            int minutes =
                (int) Math.min(available, Math.min(sessionMinutes, remaining.get(task.id())));
            Instant end = cursor.plusSeconds(minutes * 60L);
            blocks.add(
                new PlanProposal.Block(task.id(), task.subjectId(), task.title(), cursor, end));
            remaining.put(task.id(), remaining.get(task.id()) - minutes);
            cursor = end.plusSeconds(breakMinutes * 60L);
            nextStart = cursor;
          }
        }
      }
    }
    return new Result(
        List.copyOf(blocks),
        tasks.stream()
            .filter(t -> remaining.get(t.id()) > 0)
            .map(t -> new PlanProposal.Unscheduled(t.id(), t.title(), remaining.get(t.id())))
            .toList());
  }
}
