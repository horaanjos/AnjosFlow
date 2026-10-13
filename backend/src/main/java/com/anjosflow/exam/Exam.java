package com.anjosflow.exam;

import com.anjosflow.common.model.AuditedEntity;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.*;

@Entity
@Table(name = "exams")
public class Exam extends AuditedEntity {
  @Column(nullable = false, length = 150)
  private String title;

  @Column(length = 1000)
  private String description;

  private Long subjectId;

  @Column(nullable = false)
  private Instant scheduledAt;

  @Column(length = 200)
  private String location;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private ExamStatus status;

  @Column(precision = 8, scale = 2)
  private BigDecimal score;

  @Column(precision = 8, scale = 2)
  private BigDecimal maxScore;

  public Exam() {}

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

  public Instant getScheduledAt() {
    return scheduledAt;
  }

  public void setScheduledAt(Instant value) {
    this.scheduledAt = value;
  }

  public String getLocation() {
    return location;
  }

  public void setLocation(String value) {
    this.location = value;
  }

  public ExamStatus getStatus() {
    return status;
  }

  public void setStatus(ExamStatus value) {
    this.status = value;
  }

  public BigDecimal getScore() {
    return score;
  }

  public void setScore(BigDecimal value) {
    this.score = value;
  }

  public BigDecimal getMaxScore() {
    return maxScore;
  }

  public void setMaxScore(BigDecimal value) {
    this.maxScore = value;
  }
}
