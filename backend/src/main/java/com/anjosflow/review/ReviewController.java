package com.anjosflow.review;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {
  private final ReviewService service;

  public ReviewController(ReviewService service) {
    this.service = service;
  }

  @PostMapping
  public ResponseEntity<ReviewResponse> create(@Valid @RequestBody ReviewRequest request) {
    var response = service.create(request);
    return ResponseEntity.created(URI.create("/api/reviews/" + response.id())).body(response);
  }

  @GetMapping
  public List<ReviewResponse> list() {
    return service.list();
  }

  @GetMapping("/{id}")
  public ReviewResponse find(@PathVariable Long id) {
    return service.find(id);
  }

  @PutMapping("/{id}")
  public ReviewResponse update(@PathVariable Long id, @Valid @RequestBody ReviewRequest request) {
    return service.update(id, request);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable Long id) {
    service.delete(id);
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/{id}/complete")
  public ReviewResponse complete(@PathVariable Long id) {
    return service.complete(id);
  }
}
