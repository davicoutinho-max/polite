package dev.civicpulse.participation.adapter.in.web.dto;

import dev.civicpulse.participation.domain.model.Petition;
import java.time.LocalDate;
import java.util.UUID;

public record PetitionResponse(UUID id, String title, String summary, String category, int goal, int signaturesCount, LocalDate deadline) {

  public static PetitionResponse from(Petition petition) {
    return new PetitionResponse(
        petition.id(), petition.title(), petition.summary().orElse(null), petition.category().orElse(null), petition.goal(),
        petition.signaturesCount(), petition.deadline().orElse(null));
  }
}
