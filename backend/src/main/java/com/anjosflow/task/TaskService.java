package com.anjosflow.task;

import com.anjosflow.common.error.ApiException;
import com.anjosflow.common.model.References;
import com.anjosflow.common.time.AppTime;
import jakarta.persistence.criteria.Predicate;
import java.time.LocalDate;
import java.util.*;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class TaskService {
  private final TaskRepository repository;
  private final References references;
  private final AppTime time;

  public TaskService(TaskRepository repository, References references, AppTime time) {
    this.repository = repository;
    this.references = references;
    this.time = time;
  }

  @Transactional
  public TaskResponse create(TaskRequest request) {
    Task task = new Task();
    apply(task, request);
    return TaskResponse.from(repository.saveAndFlush(task));
  }

  public List<TaskResponse> list(
      Long subjectId, TaskStatus status, Priority priority, LocalDate dueBefore) {
    return repository
        .findAll(
            (root, query, cb) -> {
              List<Predicate> filters = new ArrayList<>();
              if (subjectId != null) filters.add(cb.equal(root.get("subjectId"), subjectId));
              if (status != null) filters.add(cb.equal(root.get("status"), status));
              if (priority != null) filters.add(cb.equal(root.get("priority"), priority));
              if (dueBefore != null)
                filters.add(cb.lessThanOrEqualTo(root.get("dueDate"), dueBefore));
              return cb.and(filters.toArray(Predicate[]::new));
            },
            Sort.by("dueDate", "id"))
        .stream()
        .map(TaskResponse::from)
        .toList();
  }

  public TaskResponse find(Long id) {
    return TaskResponse.from(entity(id));
  }

  @Transactional
  public TaskResponse update(Long id, TaskRequest request) {
    Task task = entity(id);
    apply(task, request);
    return TaskResponse.from(repository.saveAndFlush(task));
  }

  @Transactional
  public TaskResponse status(Long id, TaskStatus status) {
    Task task = entity(id);
    transition(task, status);
    return TaskResponse.from(repository.saveAndFlush(task));
  }

  @Transactional
  public void delete(Long id) {
    repository.delete(entity(id));
    repository.flush();
  }

  public Task entity(Long id) {
    return repository.findById(id).orElseThrow(() -> ApiException.missing("Tarefa"));
  }

  private void apply(Task task, TaskRequest r) {
    references.subject(r.subjectId());
    task.setTitle(r.title());
    task.setDescription(r.description());
    task.setSubjectId(r.subjectId());
    task.setPriority(r.priority());
    task.setDueDate(r.dueDate());
    task.setEstimatedMinutes(r.estimatedMinutes());
    transition(task, r.status());
  }

  private void transition(Task task, TaskStatus status) {
    if (status == TaskStatus.COMPLETED && task.getStatus() != TaskStatus.COMPLETED)
      task.setCompletedAt(time.now());
    if (status != TaskStatus.COMPLETED) task.setCompletedAt(null);
    task.setStatus(status);
  }
}
