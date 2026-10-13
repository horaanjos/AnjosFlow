package com.anjosflow.exam;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/exams")
public class ExamController {
  private final ExamService service;

  public ExamController(ExamService service) {
    this.service = service;
  }

  @PostMapping
  public ResponseEntity<ExamResponse> create(@Valid @RequestBody ExamRequest request) {
    var response = service.create(request);
    return ResponseEntity.created(URI.create("/api/exams/" + response.id())).body(response);
  }

  @GetMapping
  public List<ExamResponse> list() {
    return service.list();
  }

  @GetMapping("/{id}")
  public ExamResponse find(@PathVariable Long id) {
    return service.find(id);
  }

  @PutMapping("/{id}")
  public ExamResponse update(@PathVariable Long id, @Valid @RequestBody ExamRequest request) {
    return service.update(id, request);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable Long id) {
    service.delete(id);
    return ResponseEntity.noContent().build();
  }
}
