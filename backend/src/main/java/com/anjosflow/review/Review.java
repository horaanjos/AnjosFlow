package com.anjosflow.review;

import com.anjosflow.common.model.AuditedEntity;
import jakarta.persistence.*;
import java.time.*;

@Entity
@Table(name = "reviews")
public class Review extends AuditedEntity {
  @Column(nullable = false)
  private Long subjectId;

  @Column(nullable = false, length = 150)
  private String title;

  @Column(length = 1000)
  private String notes;

  private Instant lastReviewedAt;

  @Column(nullable = false)
  private LocalDate nextReviewAt;

  @Column(nullable = false)
  private Integer intervalDays;

  @Column(nullable = false)
  private Integer reviewCount;

  public Review() {}

  public Long getSubjectId() {
    return subjectId;
  }

  public void setSubjectId(Long value) {
    this.subjectId = value;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String value) {
    this.title = value;
  }

  public String getNotes() {
    return notes;
  }

  public void setNotes(String value) {
    this.notes = value;
  }

  public Instant getLastReviewedAt() {
    return lastReviewedAt;
  }

  public void setLastReviewedAt(Instant value) {
    this.lastReviewedAt = value;
  }

  public LocalDate getNextReviewAt() {
    return nextReviewAt;
  }

  public void setNextReviewAt(LocalDate value) {
    this.nextReviewAt = value;
  }

  public Integer getIntervalDays() {
    return intervalDays;
  }

  public void setIntervalDays(Integer value) {
    this.intervalDays = value;
  }

  public Integer getReviewCount() {
    return reviewCount;
  }

  public void setReviewCount(Integer value) {
    this.reviewCount = value;
  }
}
