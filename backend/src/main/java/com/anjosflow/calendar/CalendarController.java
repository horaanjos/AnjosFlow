package com.anjosflow.calendar;

import java.time.Instant;
import java.util.List;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/calendar")
public class CalendarController {
  private final CalendarService service;

  public CalendarController(CalendarService service) {
    this.service = service;
  }

  @GetMapping
  public List<CalendarItem> list(@RequestParam Instant from, @RequestParam Instant to) {
    return service.list(from, to);
  }
}
