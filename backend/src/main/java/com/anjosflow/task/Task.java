package com.anjosflow.task;

import com.anjosflow.common.model.AuditedEntity;
import jakarta.persistence.*;
import java.time.*;

@Entity
@Table(name = "tasks")
public class Task extends AuditedEntity {
  @Column(nullable = false, length = 150)
  private String title;

  @Column(length = 1000)
  private String description;

  @Column(nullable = false)
  private Long subjectId;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private TaskStatus status;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 10)
  private Priority priority;

  private LocalDate dueDate;
  private Integer estimatedMinutes;
  private Instant completedAt;

  public Task() {}

  public String getTitle() {
    return title;
  }

  public void setTitle(String value) {
    this.title = value;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String value) {
    this.description = value;
  }

  public Long getSubjectId() {
    return subjectId;
  }

  public void setSubjectId(Long value) {
    this.subjectId = value;
  }

  public TaskStatus getStatus() {
    return status;
  }

  public void setStatus(TaskStatus value) {
    this.status = value;
  }

  public Priority getPriority() {
    return priority;
  }

  public void setPriority(Priority value) {
    this.priority = value;
  }

  public LocalDate getDueDate() {
    return dueDate;
  }

  public void setDueDate(LocalDate value) {
    this.dueDate = value;
  }

  public Integer getEstimatedMinutes() {
    return estimatedMinutes;
  }

  public void setEstimatedMinutes(Integer value) {
    this.estimatedMinutes = value;
  }

  public Instant getCompletedAt() {
    return completedAt;
  }

  public void setCompletedAt(Instant value) {
    this.completedAt = value;
  }
}
