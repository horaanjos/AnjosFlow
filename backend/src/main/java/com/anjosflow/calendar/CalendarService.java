package com.anjosflow.calendar;

import com.anjosflow.exam.*;
import java.time.Instant;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class CalendarService {
  private final CalendarEventService events;
  private final ExamRepository exams;

  public CalendarService(CalendarEventService events, ExamRepository exams) {
    this.events = events;
    this.exams = exams;
  }

  public List<CalendarItem> list(Instant from, Instant to) {
    List<CalendarItem> result = new ArrayList<>();
    for (var e : events.list(from, to, null))
      result.add(
          new CalendarItem(
              "event-" + e.id(),
              e.title(),
              e.description(),
              e.subjectId(),
              e.startAt(),
              e.endAt(),
              e.type(),
              "EVENT",
              e.id()));
    for (var e :
        exams.findAll(
            (root, q, cb) ->
                cb.and(
                    cb.notEqual(root.get("status"), ExamStatus.CANCELLED),
                    cb.greaterThan(root.get("scheduledAt"), from.minusSeconds(3600)),
                    cb.lessThan(root.get("scheduledAt"), to))))
      result.add(
          new CalendarItem(
              "exam-" + e.getId(),
              e.getTitle(),
              e.getDescription(),
              e.getSubjectId(),
              e.getScheduledAt(),
              e.getScheduledAt().plusSeconds(3600),
              EventType.EXAM,
              "EXAM",
              e.getId()));
    return result.stream()
        .sorted(Comparator.comparing(CalendarItem::startAt).thenComparing(CalendarItem::id))
        .toList();
  }
}
