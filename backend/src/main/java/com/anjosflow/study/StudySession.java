package com.anjosflow.study;

import com.anjosflow.common.model.AuditedEntity;
import jakarta.persistence.*;
import java.time.*;
import java.util.UUID;

@Entity
@Table(name = "study_sessions")
public class StudySession extends AuditedEntity {
  @Column(nullable = false)
  private Long subjectId;

  private Long taskId;

  @Column(nullable = false)
  private Instant startedAt;

  @Column(nullable = false)
  private Instant endedAt;

  @Column(length = 1000)
  private String notes;

  @Column(nullable = false, unique = true)
  private UUID clientKey;

  @Column(nullable = false)
  private Integer durationMinutes;

  public StudySession() {}

  public Long getSubjectId() {
    return subjectId;
  }

  public void setSubjectId(Long value) {
    this.subjectId = value;
  }

  public Long getTaskId() {
    return taskId;
  }

  public void setTaskId(Long value) {
    this.taskId = value;
  }

  public Instant getStartedAt() {
    return startedAt;
  }

  public void setStartedAt(Instant value) {
    this.startedAt = value;
  }

  public Instant getEndedAt() {
    return endedAt;
  }

  public void setEndedAt(Instant value) {
    this.endedAt = value;
  }

  public String getNotes() {
    return notes;
  }

  public void setNotes(String value) {
    this.notes = value;
  }

  public UUID getClientKey() {
    return clientKey;
  }

  public void setClientKey(UUID value) {
    this.clientKey = value;
  }

  public Integer getDurationMinutes() {
    return durationMinutes;
  }

  public void setDurationMinutes(Integer value) {
    this.durationMinutes = value;
  }
}
