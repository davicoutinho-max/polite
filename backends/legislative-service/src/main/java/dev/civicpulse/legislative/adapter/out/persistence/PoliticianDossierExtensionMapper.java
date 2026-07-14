package dev.civicpulse.legislative.adapter.out.persistence;

import dev.civicpulse.legislative.domain.model.PoliticianDossierExtension;
import org.springframework.stereotype.Component;

@Component
class PoliticianDossierExtensionMapper {

  PoliticianDossierExtension toDomain(PoliticianDossierExtensionJpaEntity entity) {
    return PoliticianDossierExtension.reconstitute(
        entity.getPoliticianAccountId(),
        entity.getEducation(),
        entity.getProfession(),
        entity.getPatrimony(),
        entity.getEmail(),
        entity.getPhone(),
        entity.getOfficeDetail(),
        entity.getSpeechesCount(),
        entity.getInterviewsCount(),
        entity.getTripsCount(),
        entity.getCreatedAt());
  }

  PoliticianDossierExtensionJpaEntity toEntity(PoliticianDossierExtension dossier) {
    return new PoliticianDossierExtensionJpaEntity(
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
