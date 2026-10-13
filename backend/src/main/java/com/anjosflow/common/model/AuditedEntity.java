package com.anjosflow.common.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@MappedSuperclass
public abstract class AuditedEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  protected Long id;

  @Column(name = "created_at", nullable = false, updatable = false)
  protected Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  protected Instant updatedAt;

  @PrePersist
  protected void createAudit() {
    createdAt = Instant.now().truncatedTo(ChronoUnit.MICROS);
    updatedAt = createdAt;
  }

  @PreUpdate
  protected void updateAudit() {
    updatedAt = Instant.now().truncatedTo(ChronoUnit.MICROS);
  }

  public Long getId() {
    return id;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }
}
