package com.anjosflow;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.anjosflow.calendar.*;
import com.anjosflow.common.error.ApiException;
import com.anjosflow.common.model.References;
import com.anjosflow.common.time.AppTime;
import com.anjosflow.exam.*;
import com.anjosflow.goal.*;
import com.anjosflow.review.*;
import com.anjosflow.study.*;
import com.anjosflow.task.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.jdbc.core.JdbcTemplate;

class MvpRulesTest {
  private final AppTime time =
      new AppTime(Clock.fixed(Instant.parse("2026-10-13T02:30:00Z"), ZoneOffset.UTC));
  private final References refs = mock(References.class);

  @Test
  void dayBoundariesUseSaoPauloAndUtc() {
    assertThat(time.today()).isEqualTo(LocalDate.of(2026, 10, 12));
    assertThat(time.start(time.today())).isEqualTo(Instant.parse("2026-10-12T03:00:00Z"));
  }

  @Test
  void completingReopeningAndRepeatedCompletionHaveConsistentAudit() {
    var repo = mock(TaskRepository.class);
    var service = new TaskService(repo, refs, time);
    var task = new Task();
    task.setStatus(TaskStatus.PENDING);
    when(repo.findById(1L)).thenReturn(Optional.of(task));
    when(repo.saveAndFlush(task)).thenReturn(task);
    service.status(1L, TaskStatus.COMPLETED);
    assertThat(task.getCompletedAt()).isEqualTo(time.now());
    service.status(1L, TaskStatus.COMPLETED);
    assertThat(task.getCompletedAt()).isEqualTo(time.now());
    service.status(1L, TaskStatus.IN_PROGRESS);
    assertThat(task.getCompletedAt()).isNull();
  }

  @Test
  void taskChecksReferencedSubject() {
    var repo = mock(TaskRepository.class);
    var service = new TaskService(repo, refs, time);
    doThrow(ApiException.missing("Disciplina")).when(refs).subject(9L);
    assertThatThrownBy(
            () ->
                service.create(
                    new TaskRequest("Java", null, 9L, TaskStatus.PENDING, Priority.HIGH, null, 25)))
        .isInstanceOf(ApiException.class);
    verify(repo, never()).saveAndFlush(any());
  }

  @Test
  void invalidEventRangeIsRejected() {
    var service = new CalendarEventService(mock(CalendarEventRepository.class), refs);
    assertThatThrownBy(
            () ->
                service.create(
                    new CalendarEventRequest(
                        "Evento", null, null, time.now(), time.now(), EventType.STUDY)))
        .isInstanceOf(ApiException.class);
  }

  @Test
  void eventsCanSpanMultipleDays() {
    var repo = mock(CalendarEventRepository.class);
    when(repo.saveAndFlush(any())).thenAnswer(c -> c.getArgument(0));
    var result =
        new CalendarEventService(repo, refs)
            .create(
                new CalendarEventRequest(
                    "Viagem",
                    null,
                    null,
                    time.now(),
                    time.now().plusSeconds(172800),
                    EventType.PERSONAL));
    assertThat(result.endAt()).isAfter(result.startAt());
  }

  @Test
  void incompatibleTaskAndSessionIsRejected() {
    var tasks = mock(TaskRepository.class);
    var task = new Task();
    task.setSubjectId(2L);
    when(tasks.findById(3L)).thenReturn(Optional.of(task));
    var service =
        new StudySessionService(
            mock(StudySessionRepository.class), tasks, refs, mock(JdbcTemplate.class), time);
    assertThatThrownBy(
            () ->
                service.create(
                    new StudySessionRequest(
                        1L,
                        3L,
                        time.now().minusSeconds(1500),
                        time.now(),
                        null,
                        UUID.randomUUID())))
        .isInstanceOf(ApiException.class)
        .hasMessageContaining("mesma disciplina");
  }

  @Test
  void zeroDurationIsRejected() {
    var service =
        new StudySessionService(
            mock(StudySessionRepository.class),
            mock(TaskRepository.class),
            refs,
            mock(JdbcTemplate.class),
            time);
    assertThatThrownBy(
            () ->
                service.create(
                    new StudySessionRequest(
                        1L, null, time.now(), time.now(), null, UUID.randomUUID())))
        .isInstanceOf(ApiException.class);
  }

  @Test
  void invalidExamScoreIsRejected() {
    var service = new ExamService(mock(ExamRepository.class), refs);
    assertThatThrownBy(
            () ->
                service.create(
                    new ExamRequest(
                        "Prova",
                        null,
                        null,
                        time.now(),
                        null,
                        ExamStatus.COMPLETED,
                        new BigDecimal("11"),
                        BigDecimal.TEN)))
        .isInstanceOf(ApiException.class);
  }

  @Test
  void goalMustCoverSevenDays() {
    var service =
        new StudyGoalService(mock(StudyGoalRepository.class), refs, mock(JdbcTemplate.class));
    assertThatThrownBy(
            () ->
                service.create(
                    new StudyGoalRequest(
                        GoalType.STUDY_MINUTES, 30, time.today(), time.today().plusDays(7), null)))
        .isInstanceOf(ApiException.class);
  }

  @ParameterizedTest
  @CsvSource({"0,1", "1,3", "2,7", "3,14", "4,30", "5,30", "50,30"})
  void spacedReviewIntervalsStayAtThirtyDaysAfterFourthReview(int count, int expected) {
    assertThat(ReviewService.intervalAfter(count)).isEqualTo(expected);
  }

  @Test
  void sameDayReviewCompletionIsIdempotent() {
    var repo = mock(ReviewRepository.class);
    var review = new Review();
    review.setReviewCount(0);
    review.setIntervalDays(1);
    review.setNextReviewAt(time.today().plusDays(1));
    when(repo.findById(1L)).thenReturn(Optional.of(review));
    when(repo.saveAndFlush(review)).thenReturn(review);
    var service = new ReviewService(repo, refs, time);
    service.complete(1L);
    service.complete(1L);
    assertThat(review.getReviewCount()).isEqualTo(1);
    assertThat(review.getNextReviewAt()).isEqualTo(time.today().plusDays(3));
    verify(repo, times(1)).saveAndFlush(review);
  }

  @Test
  void corsAllowsOnlyTheConfiguredLocalOrigins() {
    class Registry extends org.springframework.web.servlet.config.annotation.CorsRegistry {
      java.util.Map<String, org.springframework.web.cors.CorsConfiguration> configurations() {
        return getCorsConfigurations();
      }
    }
    var registry = new Registry();
    new com.anjosflow.config.WebConfig("http://localhost:5173").addCorsMappings(registry);
    var cors = registry.configurations().get("/api/**");
    assertThat(cors.checkOrigin("http://localhost:5173")).isEqualTo("http://localhost:5173");
    assertThat(cors.checkOrigin("http://127.0.0.1:5173")).isEqualTo("http://127.0.0.1:5173");
    assertThat(cors.checkOrigin("https://example.com")).isNull();
    assertThat(cors.checkOrigin("http://localhost:9999")).isNull();
  }
}
