package com.anjosflow.common.model;

import com.anjosflow.common.error.ApiException;
import com.anjosflow.subject.SubjectRepository;
import org.springframework.stereotype.Component;

@Component
public class References {
  private final SubjectRepository subjects;

  public References(SubjectRepository subjects) {
    this.subjects = subjects;
  }

  public void subject(Long id) {
    if (id != null && !subjects.existsById(id)) throw ApiException.missing("Disciplina");
  }
}
