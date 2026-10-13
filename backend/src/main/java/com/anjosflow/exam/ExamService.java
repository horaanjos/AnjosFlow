package com.anjosflow.exam;

import com.anjosflow.common.error.ApiException;
import com.anjosflow.common.model.References;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ExamService {
  private final ExamRepository repository;
  private final References references;

  public ExamService(ExamRepository repository, References references) {
    this.repository = repository;
    this.references = references;
  }

  @Transactional
  public ExamResponse create(ExamRequest r) {
    var e = new Exam();
    apply(e, r);
    return ExamResponse.from(repository.saveAndFlush(e));
  }

  public List<ExamResponse> list() {
    return repository.findAll(Sort.by("scheduledAt", "id")).stream()
        .map(ExamResponse::from)
        .toList();
  }

  public ExamResponse find(Long id) {
    return ExamResponse.from(entity(id));
  }

  @Transactional
  public ExamResponse update(Long id, ExamRequest r) {
    var e = entity(id);
    apply(e, r);
    return ExamResponse.from(repository.saveAndFlush(e));
  }

  @Transactional
  public void delete(Long id) {
    repository.delete(entity(id));
  }

  private Exam entity(Long id) {
    return repository.findById(id).orElseThrow(() -> ApiException.missing("Prova"));
  }

  private void apply(Exam e, ExamRequest r) {
    if (r.score() != null && (r.maxScore() == null || r.score().compareTo(r.maxScore()) > 0))
      throw ApiException.invalid(
          "A nota não pode superar a pontuação máxima, que deve ser informada.");
    references.subject(r.subjectId());
    e.setTitle(r.title());
    e.setDescription(r.description());
    e.setSubjectId(r.subjectId());
    e.setScheduledAt(r.scheduledAt());
    e.setLocation(r.location());
    e.setStatus(r.status());
    e.setScore(r.score());
    e.setMaxScore(r.maxScore());
  }
}
