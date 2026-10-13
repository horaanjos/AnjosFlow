package com.anjosflow.study;

import jakarta.validation.Valid;
import java.net.URI;
import java.time.Instant;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sessions")
public class StudySessionController {
  private final StudySessionService service;

  public StudySessionController(StudySessionService service) {
    this.service = service;
  }

  @PostMapping
  public ResponseEntity<StudySessionResponse> create(
      @Valid @RequestBody StudySessionRequest request) {
    var r = service.create(request);
    return ResponseEntity.created(URI.create("/api/sessions/" + r.id())).body(r);
  }

  @GetMapping
  public List<StudySessionResponse> list(
      @RequestParam(required = false) Instant from,
      @RequestParam(required = false) Instant to,
      @RequestParam(required = false) Long subjectId) {
    return service.list(from, to, subjectId);
  }

  @GetMapping("/{id}")
  public StudySessionResponse find(@PathVariable Long id) {
    return service.find(id);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable Long id) {
    service.delete(id);
    return ResponseEntity.noContent().build();
  }
}
