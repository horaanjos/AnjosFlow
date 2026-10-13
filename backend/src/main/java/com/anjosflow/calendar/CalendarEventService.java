package com.anjosflow.calendar;

import com.anjosflow.common.error.ApiException;
import com.anjosflow.common.model.References;
import jakarta.persistence.criteria.Predicate;
import java.time.Instant;
import java.util.*;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class CalendarEventService {
  private final CalendarEventRepository repository;
  private final References references;

  public CalendarEventService(CalendarEventRepository repository, References references) {
    this.repository = repository;
    this.references = references;
  }

  @Transactional
  public CalendarEventResponse create(CalendarEventRequest r) {
    CalendarEvent e = new CalendarEvent();
    apply(e, r);
    return CalendarEventResponse.from(repository.saveAndFlush(e));
  }

  public List<CalendarEventResponse> list(Instant from, Instant to, Long subjectId) {
    if (from != null && to != null && !to.isAfter(from))
      throw ApiException.invalid("O fim do intervalo deve ser posterior ao início.");
    return repository
        .findAll(
            (root, q, cb) -> {
              List<Predicate> p = new ArrayList<>();
              if (from != null) p.add(cb.greaterThan(root.get("endAt"), from));
              if (to != null) p.add(cb.lessThan(root.get("startAt"), to));
              if (subjectId != null) p.add(cb.equal(root.get("subjectId"), subjectId));
              return cb.and(p.toArray(Predicate[]::new));
            },
            Sort.by("startAt", "id"))
        .stream()
        .map(CalendarEventResponse::from)
        .toList();
  }

  public CalendarEventResponse find(Long id) {
    return CalendarEventResponse.from(entity(id));
  }

  @Transactional
  public CalendarEventResponse update(Long id, CalendarEventRequest r) {
    var e = entity(id);
    apply(e, r);
    return CalendarEventResponse.from(repository.saveAndFlush(e));
  }

  @Transactional
  public void delete(Long id) {
    repository.delete(entity(id));
    repository.flush();
  }

  private CalendarEvent entity(Long id) {
    return repository.findById(id).orElseThrow(() -> ApiException.missing("Evento"));
  }

  private void apply(CalendarEvent e, CalendarEventRequest r) {
    if (!r.endAt().isAfter(r.startAt()))
      throw ApiException.invalid("O horário final deve ser posterior ao inicial.");
    references.subject(r.subjectId());
    if (!java.util.Objects.equals(e.getSubjectId(), r.subjectId())) e.setTaskId(null);
    e.setTitle(r.title());
    e.setDescription(r.description());
    e.setSubjectId(r.subjectId());
    e.setStartAt(r.startAt());
    e.setEndAt(r.endAt());
    e.setType(r.type());
  }
}
