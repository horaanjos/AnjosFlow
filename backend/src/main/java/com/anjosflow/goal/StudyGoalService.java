package com.anjosflow.goal;

import com.anjosflow.common.error.ApiException;
import com.anjosflow.common.model.References;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class StudyGoalService {
  private final StudyGoalRepository repository;
  private final References references;
  private final JdbcTemplate jdbc;

  public StudyGoalService(
      StudyGoalRepository repository, References references, JdbcTemplate jdbc) {
    this.repository = repository;
    this.references = references;
    this.jdbc = jdbc;
  }

  @Transactional
  public StudyGoalResponse create(StudyGoalRequest r) {
    var e = new StudyGoal();
    apply(e, r);
    return StudyGoalResponse.from(repository.saveAndFlush(e));
  }

  public List<StudyGoalResponse> list() {
    return repository.findAll(Sort.by(Sort.Direction.DESC, "periodStart", "id")).stream()
        .map(StudyGoalResponse::from)
        .toList();
  }

  public StudyGoalResponse find(Long id) {
    return StudyGoalResponse.from(entity(id));
  }

  @Transactional
  public StudyGoalResponse update(Long id, StudyGoalRequest r) {
    var e = entity(id);
    apply(e, r);
    return StudyGoalResponse.from(repository.saveAndFlush(e));
  }

  @Transactional
  public void delete(Long id) {
    repository.delete(entity(id));
  }

  private StudyGoal entity(Long id) {
    return repository.findById(id).orElseThrow(() -> ApiException.missing("Meta"));
  }

  private void apply(StudyGoal e, StudyGoalRequest r) {
    if (!r.periodEnd().equals(r.periodStart().plusDays(6)))
      throw ApiException.invalid("Uma meta semanal deve ter sete dias, incluindo início e fim.");
    references.subject(r.subjectId());
    e.setType(r.type());
    e.setTargetValue(r.targetValue());
    e.setPeriodStart(r.periodStart());
    e.setPeriodEnd(r.periodEnd());
    e.setSubjectId(r.subjectId());
  }

  public List<GoalProgress> progress() {
    // Uma consulta agregada para todas as metas, sem consulta HTTP/JDBC por meta.
    var values =
        jdbc.query(
            "SELECT g.id, COALESCE(CASE WHEN g.type='STUDY_MINUTES' THEN (SELECT"
                + " SUM(s.duration_minutes) FROM study_sessions s WHERE s.ended_at >="
                + " (g.period_start::timestamp AT TIME ZONE 'America/Sao_Paulo') AND s.ended_at <"
                + " ((g.period_end+1)::timestamp AT TIME ZONE 'America/Sao_Paulo') AND"
                + " (g.subject_id IS NULL OR s.subject_id=g.subject_id)) ELSE (SELECT COUNT(*) FROM"
                + " tasks t WHERE t.status='COMPLETED' AND t.completed_at >="
                + " (g.period_start::timestamp AT TIME ZONE 'America/Sao_Paulo') AND t.completed_at"
                + " < ((g.period_end+1)::timestamp AT TIME ZONE 'America/Sao_Paulo') AND"
                + " (g.subject_id IS NULL OR t.subject_id=g.subject_id)) END,0) AS value FROM"
                + " study_goals g",
            (rs, n) -> java.util.Map.entry(rs.getLong("id"), rs.getLong("value")));
    var byId =
        values.stream()
            .collect(
                java.util.stream.Collectors.toMap(
                    java.util.Map.Entry::getKey, java.util.Map.Entry::getValue));
    return repository.findAll(Sort.by("periodStart", "id")).stream()
        .map(
            g -> {
              long current = byId.getOrDefault(g.getId(), 0L);
              return new GoalProgress(
                  StudyGoalResponse.from(g),
                  current,
                  (int) Math.min(100, current * 100 / g.getTargetValue()));
            })
        .toList();
  }
}
