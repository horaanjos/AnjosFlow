package com.anjosflow.task;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {
  private final TaskService service;

  public TaskController(TaskService service) {
    this.service = service;
  }

  public record StatusRequest(@NotNull TaskStatus status) {}

  @PostMapping
  public ResponseEntity<TaskResponse> create(@Valid @RequestBody TaskRequest request) {
    var result = service.create(request);
    return ResponseEntity.created(URI.create("/api/tasks/" + result.id())).body(result);
  }

  @GetMapping
  public List<TaskResponse> list(
      @RequestParam(required = false) Long subjectId,
      @RequestParam(required = false) TaskStatus status,
      @RequestParam(required = false) Priority priority,
      @RequestParam(required = false) LocalDate dueBefore) {
    return service.list(subjectId, status, priority, dueBefore);
  }

  @GetMapping("/{id}")
  public TaskResponse find(@PathVariable Long id) {
    return service.find(id);
  }

  @PutMapping("/{id}")
  public TaskResponse update(@PathVariable Long id, @Valid @RequestBody TaskRequest request) {
    return service.update(id, request);
  }

  @PatchMapping("/{id}/status")
  public TaskResponse status(@PathVariable Long id, @Valid @RequestBody StatusRequest request) {
    return service.status(id, request.status());
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable Long id) {
    service.delete(id);
    return ResponseEntity.noContent().build();
  }
}
