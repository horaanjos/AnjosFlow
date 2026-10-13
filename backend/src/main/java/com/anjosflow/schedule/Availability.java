package com.anjosflow.schedule;

import com.anjosflow.common.model.AuditedEntity;
import jakarta.persistence.*;
import java.time.LocalTime;

@Entity
@Table(name = "study_availability")
public class Availability extends AuditedEntity {
  @Column(nullable = false)
  private Integer dayOfWeek;

  @Column(nullable = false)
  private LocalTime startTime;

  @Column(nullable = false)
  private LocalTime endTime;

  public Availability() {}

  public Availability(AvailabilitySlot slot) {
    dayOfWeek = slot.dayOfWeek();
    startTime = slot.startTime();
    endTime = slot.endTime();
  }

  public AvailabilitySlot toSlot() {
    return new AvailabilitySlot(dayOfWeek, startTime, endTime);
  }
}
