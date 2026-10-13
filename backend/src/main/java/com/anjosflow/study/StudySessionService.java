package com.anjosflow.study;

import com.anjosflow.common.error.ApiException;
import com.anjosflow.common.model.References;
import com.anjosflow.common.time.AppTime;
import com.anjosflow.task.TaskRepository;
import jakarta.persistence.criteria.Predicate;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;
import org.springframework.data.domain.Sort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class StudySessionService {
  private final StudySessionRepository repository;
  private final TaskRepository tasks;
  private final References references;
  private final JdbcTemplate jdbc;
  private final AppTime time;

  public StudySessionService(
      StudySessionRepository repository,
      TaskRepository tasks,
      References references,
      JdbcTemplate jdbc,
      AppTime time) {
    this.repository = repository;
    this.tasks = tasks;
    this.references = references;
    this.jdbc = jdbc;
    this.time = time;
  }

  @Transactional
  public StudySessionResponse create(StudySessionRequest r) {
    references.subject(r.subjectId());
    if (r.taskId() != null) {
      var task = tasks.findById(r.taskId()).orElseThrow(() -> ApiException.missing("Tarefa"));
      if (!Objects.equals(task.getSubjectId(), r.subjectId()))
        throw ApiException.invalid("A tarefa deve pertencer à mesma disciplina da sessão.");
    }
    long minutes = Duration.between(r.startedAt(), r.endedAt()).toMinutes();
    if (minutes < 1 || minutes > 10080)
      throw ApiException.invalid("A sessão deve durar entre 1 minuto e 7 dias.");
    if (r.endedAt().isAfter(time.now().plusSeconds(5)))
      throw ApiException.invalid("Uma sessão concluída não pode terminar no futuro.");
    // Serializa reenvios da mesma chave, inclusive em requisições concorrentes.
    jdbc.query(
        "SELECT pg_advisory_xact_lock(hashtext(?))",
        (RowCallbackHandler) rs -> {},
        r.clientKey().toString());
    var existing = repository.findByClientKey(r.clientKey());
    if (existing.isPresent()) {
      var e = existing.get();
      if (!Objects.equals(e.getSubjectId(), r.subjectId())
          || !Objects.equals(e.getTaskId(), r.taskId())
          || !e.getStartedAt().equals(r.startedAt().truncatedTo(ChronoUnit.MICROS))
          || !e.getEndedAt().equals(r.endedAt().truncatedTo(ChronoUnit.MICROS))
          || !Objects.equals(e.getNotes(), r.notes()))
        throw ApiException.conflict("Esta chave de sessão já foi usada com outros dados.");
      return StudySessionResponse.from(e);
    }
    var e = new StudySession();
    e.setSubjectId(r.subjectId());
    e.setTaskId(r.taskId());
    e.setStartedAt(r.startedAt().truncatedTo(ChronoUnit.MICROS));
    e.setEndedAt(r.endedAt().truncatedTo(ChronoUnit.MICROS));
    e.setDurationMinutes((int) minutes);
    e.setNotes(r.notes());
    e.setClientKey(r.clientKey());
    return StudySessionResponse.from(repository.saveAndFlush(e));
  }

  public List<StudySessionResponse> list(Instant from, Instant to, Long subjectId) {
    if (from != null && to != null && !to.isAfter(from))
      throw ApiException.invalid("Intervalo de consulta inválido.");
    return repository
        .findAll(
            (root, q, cb) -> {
              List<Predicate> p = new ArrayList<>();
              if (from != null) p.add(cb.greaterThanOrEqualTo(root.get("endedAt"), from));
              if (to != null) p.add(cb.lessThan(root.get("endedAt"), to));
              if (subjectId != null) p.add(cb.equal(root.get("subjectId"), subjectId));
              return cb.and(p.toArray(Predicate[]::new));
            },
            Sort.by(Sort.Direction.DESC, "endedAt", "id"))
        .stream()
        .map(StudySessionResponse::from)
        .toList();
  }

  public StudySessionResponse find(Long id) {
    return StudySessionResponse.from(entity(id));
  }

  @Transactional
  public void delete(Long id) {
    repository.delete(entity(id));
  }

  private StudySession entity(Long id) {
    return repository.findById(id).orElseThrow(() -> ApiException.missing("Sessão"));
  }
}
