package dev.civicpulse.platformconfig.adapter.in.web;

import dev.civicpulse.platformconfig.adapter.in.web.dto.PoliticianAssignmentResponse;
import dev.civicpulse.platformconfig.adapter.in.web.dto.ReassignPoliticianRequest;
import dev.civicpulse.platformconfig.application.port.in.ManagePoliticianAssignmentUseCase;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/politician-assignments")
public class PoliticianAssignmentController {

  private final ManagePoliticianAssignmentUseCase managePoliticianAssignmentUseCase;

  public PoliticianAssignmentController(ManagePoliticianAssignmentUseCase managePoliticianAssignmentUseCase) {
    this.managePoliticianAssignmentUseCase = managePoliticianAssignmentUseCase;
  }

  @GetMapping("/{politicianAccountId}")
  public PoliticianAssignmentResponse get(@PathVariable UUID politicianAccountId) {
    return PoliticianAssignmentResponse.from(managePoliticianAssignmentUseCase.getAssignment(politicianAccountId));
  }

  @PutMapping("/{politicianAccountId}")
  public PoliticianAssignmentResponse reassign(@PathVariable UUID politicianAccountId, @Valid @RequestBody ReassignPoliticianRequest request) {
    return PoliticianAssignmentResponse.from(managePoliticianAssignmentUseCase.reassign(politicianAccountId, request.partyId()));
  }
}
