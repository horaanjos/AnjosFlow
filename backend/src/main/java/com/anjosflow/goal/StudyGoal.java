package com.anjosflow.goal;

import com.anjosflow.common.model.AuditedEntity;
import jakarta.persistence.*;
import java.time.*;

@Entity
@Table(name = "study_goals")
public class StudyGoal extends AuditedEntity {
  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private GoalType type;

  @Column(nullable = false)
  private Integer targetValue;

  @Column(nullable = false)
  private LocalDate periodStart;

  @Column(nullable = false)
  private LocalDate periodEnd;

  private Long subjectId;

  public StudyGoal() {}

  public GoalType getType() {
    return type;
  }

  public void setType(GoalType value) {
    this.type = value;
  }

  public Integer getTargetValue() {
    return targetValue;
  }

  public void setTargetValue(Integer value) {
    this.targetValue = value;
  }

  public LocalDate getPeriodStart() {
    return periodStart;
  }

  public void setPeriodStart(LocalDate value) {
    this.periodStart = value;
  }

  public LocalDate getPeriodEnd() {
    return periodEnd;
  }

  public void setPeriodEnd(LocalDate value) {
    this.periodEnd = value;
  }

  public Long getSubjectId() {
    return subjectId;
  }

  public void setSubjectId(Long value) {
    this.subjectId = value;
  }
}
