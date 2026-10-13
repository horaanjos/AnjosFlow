package com.anjosflow.dashboard;

import com.anjosflow.common.time.AppTime;
import com.anjosflow.exam.*;
import com.anjosflow.goal.StudyGoalService;
import com.anjosflow.review.*;
import java.sql.Timestamp;
import java.time.*;
import org.springframework.data.domain.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class DashboardService {
  private final JdbcTemplate jdbc;
  private final AppTime time;
  private final ExamRepository exams;
  private final ReviewRepository reviews;
  private final StudyGoalService goals;

  public DashboardService(
      JdbcTemplate jdbc,
      AppTime time,
      ExamRepository exams,
      ReviewRepository reviews,
      StudyGoalService goals) {
    this.jdbc = jdbc;
    this.time = time;
    this.exams = exams;
    this.reviews = reviews;
    this.goals = goals;
  }

  public DashboardResponse overview() {
    var today = time.today();
    var monday = today.with(DayOfWeek.MONDAY);
    var upcoming =
        exams
            .findAll(
                (r, q, cb) ->
                    cb.and(
                        cb.equal(r.get("status"), ExamStatus.UPCOMING),
                        cb.greaterThanOrEqualTo(r.get("scheduledAt"), time.now())),
                PageRequest.of(0, 5, Sort.by("scheduledAt", "id")))
            .stream()
            .map(ExamResponse::from)
            .toList();
    var next =
        reviews
            .findAll(
                (r, q, cb) -> cb.lessThanOrEqualTo(r.get("nextReviewAt"), today.plusDays(7)),
                PageRequest.of(0, 5, Sort.by("nextReviewAt", "id")))
            .stream()
            .map(ReviewResponse::from)
            .toList();
    var daily =
        jdbc.query(
            "SELECT d::date AS day, COALESCE(SUM(s.duration_minutes),0) AS minutes FROM"
                + " generate_series(?::date,?::date,interval '1 day') d LEFT JOIN study_sessions s"
                + " ON (s.ended_at AT TIME ZONE 'America/Sao_Paulo')::date=d::date GROUP BY d ORDER"
                + " BY d",
            (rs, n) ->
                new DashboardResponse.DailyStudy(
                    rs.getDate("day").toLocalDate(), rs.getLong("minutes")),
            today.minusDays(6),
            today);
    var distribution =
        jdbc.query(
            "SELECT sub.id,sub.name,sub.color,SUM(s.duration_minutes) AS minutes FROM"
                + " study_sessions s JOIN subjects sub ON sub.id=s.subject_id GROUP BY"
                + " sub.id,sub.name,sub.color ORDER BY minutes DESC,sub.id",
            (rs, n) ->
                new DashboardResponse.SubjectStudy(
                    rs.getLong("id"),
                    rs.getString("name"),
                    rs.getString("color"),
                    rs.getLong("minutes")));
    return new DashboardResponse(
        AppTime.ZONE.getId(),
        today,
        monday,
        count("SELECT COUNT(*) FROM subjects"),
        count("SELECT COUNT(*) FROM tasks WHERE status<>'COMPLETED'"),
        count("SELECT COUNT(*) FROM tasks WHERE status='COMPLETED'"),
        count("SELECT COUNT(*) FROM tasks WHERE status<>'COMPLETED' AND due_date < ?", today),
        minutes(today, today.plusDays(1)),
        minutes(monday, monday.plusDays(7)),
        daily,
        distribution,
        upcoming,
        next,
        goals.progress());
  }

  private long count(String sql, Object... args) {
    var value = jdbc.queryForObject(sql, Long.class, args);
    return value == null ? 0 : value;
  }

  private long minutes(LocalDate from, LocalDate to) {
    return count(
        "SELECT COALESCE(SUM(duration_minutes),0) FROM study_sessions WHERE ended_at>=? AND"
            + " ended_at<?",
        Timestamp.from(time.start(from)),
        Timestamp.from(time.start(to)));
  }
}
