package dev.civicpulse.participation.adapter.out.persistence;

import dev.civicpulse.participation.domain.model.PetitionSignature;
import org.springframework.stereotype.Component;

@Component
class PetitionSignatureMapper {

  PetitionSignature toDomain(PetitionSignatureJpaEntity entity) {
    return PetitionSignature.reconstitute(entity.getPetitionId(), entity.getCitizenAccountId(), entity.getSignedAt());
  }

  PetitionSignatureJpaEntity toEntity(PetitionSignature signature) {
    return new PetitionSignatureJpaEntity(signature.petitionId(), signature.citizenAccountId(), signature.signedAt());
  }
}
