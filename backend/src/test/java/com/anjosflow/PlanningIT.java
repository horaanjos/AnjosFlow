package com.anjosflow;

import static org.assertj.core.api.Assertions.*;

import com.anjosflow.calendar.*;
import com.anjosflow.common.error.ApiException;
import com.anjosflow.common.time.AppTime;
import com.anjosflow.schedule.*;
import com.anjosflow.subject.*;
import com.anjosflow.task.*;
import java.time.*;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class PlanningIT {
  @Autowired PlanningService planning;
  @Autowired SubjectRepository subjects;
  @Autowired TaskRepository tasks;
  @Autowired CalendarEventRepository events;

  // A transação de teste é revertida integralmente: não altera a configuração do usuário.
  private LocalDate prepare() {
    var week = LocalDate.now(AppTime.ZONE).with(DayOfWeek.MONDAY).plusWeeks(52);
    var subject = subjects.saveAndFlush(new Subject("IT-Planner " + UUID.randomUUID(), null, null));
    var task = new Task();
    task.setTitle("IT-Prioridade " + UUID.randomUUID());
    task.setSubjectId(subject.getId());
    task.setStatus(TaskStatus.PENDING);
    task.setPriority(Priority.HIGH);
    task.setDueDate(LocalDate.of(1900, 1, 1));
    task.setEstimatedMinutes(30);
    tasks.saveAndFlush(task);
    planning.saveAvailability(
        List.of(new AvailabilitySlot(1, LocalTime.of(9, 0), LocalTime.of(10, 0))));
    return week;
  }

  @Test
  void savesOnlyAcceptedPlanAndReplaysWithoutDuplicateEvents() {
    var week = prepare();
    long initial = events.count();
    var preview = planning.preview(new PlanRequest(week, 25, 5, null, null));
    assertThat(preview.blocks()).isNotEmpty();
    assertThat(events.count()).isEqualTo(initial);
    var request = new PlanRequest(week, 25, 5, UUID.randomUUID(), preview.fingerprint());
    var accepted = planning.accept(request);
    assertThat(accepted).hasSameSizeAs(preview.blocks());
    var replay = planning.accept(request);
    assertThat(replay)
        .extracting(CalendarEventResponse::id)
        .containsExactlyElementsOf(accepted.stream().map(CalendarEventResponse::id).toList());
    assertThat(events.count()).isEqualTo(initial + accepted.size());
  }

  @Test
  void detectsChangesBetweenPreviewAndAccept() {
    var week = prepare();
    var preview = planning.preview(new PlanRequest(week, 25, 5, null, null));
    planning.saveAvailability(
        List.of(new AvailabilitySlot(1, LocalTime.of(10, 0), LocalTime.of(11, 0))));
    assertThatThrownBy(
            () ->
                planning.accept(
                    new PlanRequest(week, 25, 5, UUID.randomUUID(), preview.fingerprint())))
        .isInstanceOf(ApiException.class)
        .hasMessageContaining("dados mudaram");
  }

  @Test
  void rejectsOverlappingAndReversedAvailability() {
    assertThatThrownBy(
            () ->
                planning.saveAvailability(
                    List.of(new AvailabilitySlot(1, LocalTime.of(10, 0), LocalTime.of(9, 0)))))
        .isInstanceOf(ApiException.class);
    assertThatThrownBy(
            () ->
                planning.saveAvailability(
                    List.of(
                        new AvailabilitySlot(1, LocalTime.of(9, 0), LocalTime.of(11, 0)),
                        new AvailabilitySlot(1, LocalTime.of(10, 0), LocalTime.of(12, 0)))))
        .isInstanceOf(ApiException.class);
  }
}
