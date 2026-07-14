package dev.civicpulse.legislative.adapter.in.web;

import dev.civicpulse.legislative.adapter.in.web.dto.AddMilestoneRequest;
import dev.civicpulse.legislative.adapter.in.web.dto.CareerMilestoneResponse;
import dev.civicpulse.legislative.application.port.in.CareerUseCase;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/politicians/{politicianAccountId}/career")
public class CareerController {

  private final CareerUseCase careerUseCase;

  public CareerController(CareerUseCase careerUseCase) {
    this.careerUseCase = careerUseCase;
  }

  @GetMapping
  public List<CareerMilestoneResponse> list(@PathVariable UUID politicianAccountId) {
    return careerUseCase.getMilestones(politicianAccountId).stream().map(CareerMilestoneResponse::from).toList();
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public CareerMilestoneResponse add(@PathVariable UUID politicianAccountId, @Valid @RequestBody AddMilestoneRequest request) {
    return CareerMilestoneResponse.from(careerUseCase.addMilestone(politicianAccountId, request.year(), request.title(), request.detail()));
  }
}
