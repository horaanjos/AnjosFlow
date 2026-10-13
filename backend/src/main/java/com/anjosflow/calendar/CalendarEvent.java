package com.anjosflow.calendar;

import com.anjosflow.common.model.AuditedEntity;
import jakarta.persistence.*;
import java.time.*;

@Entity
@Table(name = "calendar_events")
public class CalendarEvent extends AuditedEntity {
  @Column(nullable = false, length = 150)
  private String title;

  @Column(length = 1000)
  private String description;

  private Long subjectId;

  @Column(nullable = false)
  private Instant startAt;

  @Column(nullable = false)
  private Instant endAt;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 10)
  private EventType type;

  private Long taskId;

  public Long getTaskId() {
    return taskId;
  }

  public void setTaskId(Long value) {
    taskId = value;
  }

  private Long planId;

  public Long getPlanId() {
    return planId;
  }

  public void setPlanId(Long value) {
    planId = value;
  }

  public CalendarEvent() {}

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

  public Instant getStartAt() {
    return startAt;
  }

  public void setStartAt(Instant value) {
    this.startAt = value;
  }

  public Instant getEndAt() {
    return endAt;
  }

  public void setEndAt(Instant value) {
    this.endAt = value;
  }

  public EventType getType() {
    return type;
  }

  public void setType(EventType value) {
    this.type = value;
  }
}
