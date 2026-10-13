package com.anjosflow.common.time;

import java.time.*;
import java.time.temporal.ChronoUnit;
import org.springframework.stereotype.Component;

@Component
public class AppTime {
  public static final ZoneId ZONE = ZoneId.of("America/Sao_Paulo");
  private final Clock clock;

  public AppTime(Clock clock) {
    this.clock = clock;
  }

  public Instant now() {
    return clock.instant().truncatedTo(ChronoUnit.MICROS);
  }

  public LocalDate today() {
    return LocalDate.now(clock.withZone(ZONE));
  }

  public Instant start(LocalDate date) {
    return date.atStartOfDay(ZONE).toInstant();
  }
}
