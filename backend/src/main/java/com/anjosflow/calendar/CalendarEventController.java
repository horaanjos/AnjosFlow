package com.anjosflow.calendar;

import jakarta.validation.Valid;
import java.net.URI;
import java.time.Instant;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/events")
public class CalendarEventController {
  private final CalendarEventService service;

  public CalendarEventController(CalendarEventService service) {
    this.service = service;
  }

  @PostMapping
  public ResponseEntity<CalendarEventResponse> create(
      @Valid @RequestBody CalendarEventRequest request) {
    var r = service.create(request);
    return ResponseEntity.created(URI.create("/api/events/" + r.id())).body(r);
  }

  @GetMapping
  public List<CalendarEventResponse> list(
      @RequestParam(required = false) Instant from,
      @RequestParam(required = false) Instant to,
      @RequestParam(required = false) Long subjectId) {
    return service.list(from, to, subjectId);
  }

  @GetMapping("/{id}")
  public CalendarEventResponse find(@PathVariable Long id) {
    return service.find(id);
  }

  @PutMapping("/{id}")
  public CalendarEventResponse update(
      @PathVariable Long id, @Valid @RequestBody CalendarEventRequest request) {
    return service.update(id, request);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable Long id) {
    service.delete(id);
    return ResponseEntity.noContent().build();
  }
}
