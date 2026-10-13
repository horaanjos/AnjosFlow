package com.anjosflow.schedule;

import com.anjosflow.calendar.CalendarEventResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.List;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/planning")
public class PlanningController {
  private final PlanningService service;

  public PlanningController(PlanningService service) {
    this.service = service;
  }

  public record AvailabilityRequest(@NotNull @Size(max = 28) List<@Valid AvailabilitySlot> slots) {}

  @GetMapping("/availability")
  public List<AvailabilitySlot> availability() {
    return service.availability();
  }

  @PutMapping("/availability")
  public List<AvailabilitySlot> save(@Valid @RequestBody AvailabilityRequest request) {
    return service.saveAvailability(request.slots());
  }

  @PostMapping("/preview")
  public PlanProposal preview(@Valid @RequestBody PlanRequest request) {
    return service.preview(request);
  }

  @PostMapping("/accept")
  public List<CalendarEventResponse> accept(@Valid @RequestBody PlanRequest request) {
    return service.accept(request);
  }
}
