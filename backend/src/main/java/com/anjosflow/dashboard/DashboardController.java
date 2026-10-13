package com.anjosflow.dashboard;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {
  private final DashboardService service;

  public DashboardController(DashboardService service) {
    this.service = service;
  }

  @GetMapping
  public DashboardResponse overview() {
    return service.overview();
  }
}
