package com.anjosflow.subject;

import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class SubjectService {
    private final SubjectRepository repository;

    public SubjectService(SubjectRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public SubjectResponse create(SubjectRequest request) {
        Subject subject = new Subject(request.name(), request.description(), request.color());
        return SubjectResponse.from(repository.saveAndFlush(subject));
    }

    public List<SubjectResponse> list() {
        return repository.findAll(Sort.by("name", "id")).stream()
                .map(SubjectResponse::from).toList();
    }

    public SubjectResponse findById(Long id) {
        return SubjectResponse.from(findEntity(id));
    }

    @Transactional
    public SubjectResponse update(Long id, SubjectRequest request) {
        Subject subject = findEntity(id);
        subject.updateDetails(request.name(), request.description(), request.color());
        // Flush executa os callbacks de auditoria antes de montar a resposta.
        return SubjectResponse.from(repository.saveAndFlush(subject));
    }

    @Transactional
    public void delete(Long id) {
        repository.delete(findEntity(id));
    }

    private Subject findEntity(Long id) {
        return repository.findById(id).orElseThrow(SubjectNotFoundException::new);
    }
}