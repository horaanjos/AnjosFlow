package com.anjosflow.schedule;

import com.anjosflow.calendar.*;
import com.anjosflow.common.error.ApiException;
import com.anjosflow.common.time.AppTime;
import com.anjosflow.exam.*;
import com.anjosflow.task.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.*;
import java.util.*;
import org.springframework.data.domain.Sort;
import org.springframework.jdbc.core.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class PlanningService {
  private final AvailabilityRepository availability;
  private final TaskRepository tasks;
  private final CalendarEventRepository events;
  private final ExamRepository exams;
  private final PlannerEngine engine;
  private final AppTime time;
  private final JdbcTemplate jdbc;

  public PlanningService(
      AvailabilityRepository availability,
      TaskRepository tasks,
      CalendarEventRepository events,
      ExamRepository exams,
      PlannerEngine engine,
      AppTime time,
      JdbcTemplate jdbc) {
    this.availability = availability;
    this.tasks = tasks;
    this.events = events;
    this.exams = exams;
    this.engine = engine;
    this.time = time;
    this.jdbc = jdbc;
  }

  public List<AvailabilitySlot> availability() {
    return availability.findAll(Sort.by("dayOfWeek", "startTime", "id")).stream()
        .map(Availability::toSlot)
        .toList();
  }

  @Transactional
  public List<AvailabilitySlot> saveAvailability(List<AvailabilitySlot> slots) {
    var sorted =
        slots.stream()
            .sorted(
                Comparator.comparing(AvailabilitySlot::dayOfWeek)
                    .thenComparing(AvailabilitySlot::startTime))
            .toList();
    for (int i = 0; i < sorted.size(); i++) {
      var s = sorted.get(i);
      if (!s.endTime().isAfter(s.startTime()))
        throw ApiException.invalid(
            "A disponibilidade deve terminar depois de começar, no mesmo dia.");
      if (i > 0
          && sorted.get(i - 1).dayOfWeek().equals(s.dayOfWeek())
          && s.startTime().isBefore(sorted.get(i - 1).endTime()))
        throw ApiException.invalid("As disponibilidades do mesmo dia não podem se sobrepor.");
    }
    availability.deleteAllInBatch();
    availability.saveAllAndFlush(sorted.stream().map(Availability::new).toList());
    return availability();
  }

  public PlanProposal preview(PlanRequest r) {
    if (r.weekStart().getDayOfWeek() != DayOfWeek.MONDAY
        || r.weekStart().isBefore(time.today().with(DayOfWeek.MONDAY)))
      throw ApiException.invalid(
          "Selecione a segunda-feira da semana atual ou de uma semana futura.");
    var from = time.start(r.weekStart());
    var to = time.start(r.weekStart().plusDays(7));
    var windows =
        availability().stream()
            .map(
                s -> {
                  var date = r.weekStart().plusDays(s.dayOfWeek() - 1);
                  Instant start = date.atTime(s.startTime()).atZone(AppTime.ZONE).toInstant();
                  Instant end = date.atTime(s.endTime()).atZone(AppTime.ZONE).toInstant();
                  if (start.isBefore(time.now()))
                    start =
                        time.now()
                            .plusSeconds(60)
                            .truncatedTo(java.time.temporal.ChronoUnit.MINUTES);
                  return new PlannerEngine.Window(start, end);
                })
            .filter(w -> w.endAt().isAfter(w.startAt()))
            .toList();
    var existing = events.findByStartAtLessThanAndEndAtGreaterThan(to, from);
    var busy = new ArrayList<PlannerEngine.Window>();
    existing.forEach(e -> busy.add(new PlannerEngine.Window(e.getStartAt(), e.getEndAt())));
    var upcoming =
        exams.findAll(
            (root, q, cb) ->
                cb.and(
                    cb.equal(root.get("status"), ExamStatus.UPCOMING),
                    cb.greaterThanOrEqualTo(root.get("scheduledAt"), from)));
    for (var e : upcoming)
      busy.add(new PlannerEngine.Window(e.getScheduledAt(), e.getScheduledAt().plusSeconds(3600)));
    Map<Long, Instant> examDates = new HashMap<>();
    for (var e : upcoming)
      if (e.getSubjectId() != null)
        examDates.merge(e.getSubjectId(), e.getScheduledAt(), (a, b) -> a.isBefore(b) ? a : b);
    var pending =
        tasks.findAll(
            (root, q, cb) -> cb.notEqual(root.get("status"), TaskStatus.COMPLETED), Sort.by("id"));
    var candidates = new ArrayList<PlannerEngine.Candidate>();
    for (var task : pending) {
      Instant deadline =
          task.getDueDate() == null ? null : time.start(task.getDueDate().plusDays(1));
      var exam = examDates.get(task.getSubjectId());
      if (exam != null && (deadline == null || exam.isBefore(deadline))) deadline = exam;
      long planned =
          existing.stream()
              .filter(e -> Objects.equals(e.getTaskId(), task.getId()))
              .mapToLong(e -> Duration.between(e.getStartAt(), e.getEndAt()).toMinutes())
              .sum();
      int remaining =
          (int)
              Math.max(
                  0,
                  (task.getEstimatedMinutes() == null
                          ? r.sessionMinutes()
                          : task.getEstimatedMinutes())
                      - planned);
      if (remaining > 0)
        candidates.add(
            new PlannerEngine.Candidate(
                task.getId(),
                task.getSubjectId(),
                task.getTitle(),
                remaining,
                task.getPriority(),
                deadline));
    }
    var result = engine.generate(windows, busy, candidates, r.sessionMinutes(), r.breakMinutes());
    return new PlanProposal(fingerprint(result), result.blocks(), result.unscheduled());
  }

  @Transactional
  public List<CalendarEventResponse> accept(PlanRequest request) {
    if (request.clientKey() == null || request.fingerprint() == null)
      throw ApiException.invalid("Informe a chave de aceite e a assinatura da sugestão.");
    // Aceites concorrentes são serializados antes de recalcular os conflitos.
    jdbc.query("SELECT pg_advisory_xact_lock(781236)", (RowCallbackHandler) rs -> {});
    jdbc.execute(
        "LOCK TABLE calendar_events, exams, tasks, study_availability IN SHARE ROW EXCLUSIVE MODE");
    var previous =
        jdbc.query(
            "SELECT id,fingerprint FROM accepted_plans WHERE client_key=?",
            (rs, n) -> Map.entry(rs.getLong("id"), rs.getString("fingerprint")),
            request.clientKey());
    if (!previous.isEmpty()) {
      if (!previous.getFirst().getValue().equals(request.fingerprint()))
        throw ApiException.conflict("Chave de aceite já usada para outra sugestão.");
      Long planId = previous.getFirst().getKey();
      return events
          .findAll((root, q, cb) -> cb.equal(root.get("planId"), planId), Sort.by("startAt", "id"))
          .stream()
          .map(CalendarEventResponse::from)
          .toList();
    }
    var proposal = preview(request);
    if (!proposal.fingerprint().equals(request.fingerprint()))
      throw ApiException.conflict(
          "Os dados mudaram. Gere e revise uma nova sugestão antes de aceitar.");
    if (proposal.blocks().isEmpty())
      throw ApiException.invalid("Não há blocos para salvar nesta sugestão.");
    Long planId =
        jdbc.queryForObject(
            "INSERT INTO accepted_plans(client_key,fingerprint) VALUES (?,?) RETURNING id",
            Long.class,
            request.clientKey(),
            request.fingerprint());
    var result = new ArrayList<CalendarEvent>();
    for (var block : proposal.blocks()) {
      var event = new CalendarEvent();
      event.setTitle(block.title());
      event.setSubjectId(block.subjectId());
      event.setStartAt(block.startAt());
      event.setEndAt(block.endAt());
      event.setType(EventType.STUDY);
      event.setPlanId(planId);
      event.setTaskId(block.taskId());
      event.setDescription("Planejamento semanal aceito");
      result.add(event);
    }
    return events.saveAllAndFlush(result).stream().map(CalendarEventResponse::from).toList();
  }

  private String fingerprint(PlannerEngine.Result result) {
    try {
      return HexFormat.of()
          .formatHex(
              MessageDigest.getInstance("SHA-256")
                  .digest(result.toString().getBytes(StandardCharsets.UTF_8)));
    } catch (java.security.NoSuchAlgorithmException e) {
      throw new IllegalStateException(e);
    }
  }
}
