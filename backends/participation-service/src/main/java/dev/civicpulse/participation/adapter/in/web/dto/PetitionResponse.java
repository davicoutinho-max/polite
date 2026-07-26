package dev.civicpulse.participation.adapter.in.web.dto;

import dev.civicpulse.participation.domain.model.Petition;
import java.time.LocalDate;
import java.util.UUID;

public record PetitionResponse(
    UUID id,
    String title,
    String summary,
    String category,
    int goal,
    int signaturesCount,
    LocalDate deadline,
    String imageUrl,
    String videoUrl,
    String fileUrl,
    String fileName,
    String petitionType) {

  public static PetitionResponse from(Petition petition) {
    return new PetitionResponse(
        petition.id(),
        petition.title(),
        petition.summary().orElse(null),
        petition.category().orElse(null),
        petition.goal(),
        petition.signaturesCount(),
        petition.deadline().orElse(null),
        petition.imageUrl().orElse(null),
        petition.videoUrl().orElse(null),
        petition.fileUrl().orElse(null),
        petition.fileName().orElse(null),
        petition.petitionType().code());
  }
}
