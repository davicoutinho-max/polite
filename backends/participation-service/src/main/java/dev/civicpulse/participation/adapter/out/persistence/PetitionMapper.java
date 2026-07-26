package dev.civicpulse.participation.adapter.out.persistence;

import dev.civicpulse.participation.domain.model.Petition;
import dev.civicpulse.participation.domain.model.PetitionType;
import org.springframework.stereotype.Component;

@Component
class PetitionMapper {

  Petition toDomain(PetitionJpaEntity entity) {
    return Petition.reconstitute(
        entity.getId(),
        entity.getTitle(),
        entity.getSummary(),
        entity.getCategory(),
        entity.getGoal(),
        entity.getSignaturesCount(),
        entity.getDeadline(),
        entity.getImageUrl(),
        entity.getVideoUrl(),
        entity.getFileUrl(),
        entity.getFileName(),
        PetitionType.fromCode(entity.getPetitionType()));
  }

  PetitionJpaEntity toEntity(Petition petition) {
    return new PetitionJpaEntity(
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
