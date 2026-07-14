package dev.civicpulse.platformconfig.adapter.in.web.dto;

import dev.civicpulse.platformconfig.domain.model.PoliticianAssignment;
import java.time.Instant;
import java.util.UUID;

public record PoliticianAssignmentResponse(UUID politicianAccountId, UUID partyId, Instant updatedAt) {

  public static PoliticianAssignmentResponse from(PoliticianAssignment assignment) {
    return new PoliticianAssignmentResponse(assignment.politicianAccountId(), assignment.partyId(), assignment.updatedAt());
  }
}
