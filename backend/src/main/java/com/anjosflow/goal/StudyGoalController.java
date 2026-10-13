package com.anjosflow.goal;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/goals")
public class StudyGoalController {
  private final StudyGoalService service;

  public StudyGoalController(StudyGoalService service) {
    this.service = service;
  }

  @PostMapping
  public ResponseEntity<StudyGoalResponse> create(@Valid @RequestBody StudyGoalRequest request) {
    var response = service.create(request);
    return ResponseEntity.created(URI.create("/api/goals/" + response.id())).body(response);
  }

  @GetMapping
  public List<StudyGoalResponse> list() {
    return service.list();
  }

  @GetMapping("/{id}")
  public StudyGoalResponse find(@PathVariable Long id) {
    return service.find(id);
  }

  @PutMapping("/{id}")
  public StudyGoalResponse update(
      @PathVariable Long id, @Valid @RequestBody StudyGoalRequest request) {
    return service.update(id, request);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable Long id) {
    service.delete(id);
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/progress")
  public List<GoalProgress> progress() {
    return service.progress();
  }
}
