package dev.civicpulse.legislative.adapter.in.web.dto;

import dev.civicpulse.legislative.domain.model.PoliticianDossierExtension;
import java.time.Instant;
import java.util.UUID;

public record DossierResponse(
    UUID politicianAccountId,
    String education,
    String profession,
    String patrimony,
    String email,
    String phone,
    String officeDetail,
    int speechesCount,
    int interviewsCount,
    int tripsCount,
    Instant createdAt) {

  public static DossierResponse from(PoliticianDossierExtension dossier) {
    return new DossierResponse(
        dossier.politicianAccountId(),
        dossier.education().orElse(null),
        dossier.profession().orElse(null),
        dossier.patrimony().orElse(null),
        dossier.email().orElse(null),
        dossier.phone().orElse(null),
        dossier.officeDetail().orElse(null),
        dossier.speechesCount(),
        dossier.interviewsCount(),
        dossier.tripsCount(),
        dossier.createdAt());
  }
}
