package com.anjosflow.calendar;

import java.util.*;
import org.springframework.data.jpa.repository.*;

public interface CalendarEventRepository
    extends JpaRepository<CalendarEvent, Long>, JpaSpecificationExecutor<CalendarEvent> {

  List<CalendarEvent> findByStartAtLessThanAndEndAtGreaterThan(
      java.time.Instant end, java.time.Instant start);
}
