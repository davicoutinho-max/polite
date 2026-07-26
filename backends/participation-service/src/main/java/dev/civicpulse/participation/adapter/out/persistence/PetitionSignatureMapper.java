package dev.civicpulse.participation.adapter.out.persistence;

import dev.civicpulse.participation.domain.model.PetitionSignature;
import org.springframework.stereotype.Component;

@Component
class PetitionSignatureMapper {

  PetitionSignature toDomain(PetitionSignatureJpaEntity entity) {
    return PetitionSignature.reconstitute(
        entity.getPetitionId(),
        entity.getCitizenAccountId(),
        entity.getSignedAt(),
        entity.getFullName(),
        entity.getCpf(),
        entity.getBirthDate(),
        entity.getCity(),
        entity.getState(),
        entity.getVerificationMethod(),
        entity.getElectoralData(),
        entity.isESignatureConsent(),
        entity.isIdentityValidated(),
        entity.getTypedSignature());
  }

  PetitionSignatureJpaEntity toEntity(PetitionSignature signature) {
    return new PetitionSignatureJpaEntity(
        signature.petitionId(),
        signature.citizenAccountId(),
        signature.signedAt(),
        signature.fullName().orElse(null),
        signature.cpf().orElse(null),
        signature.birthDate().orElse(null),
        signature.city().orElse(null),
        signature.state().orElse(null),
        signature.verificationMethod().orElse(null),
        signature.electoralData().orElse(null),
        signature.eSignatureConsent(),
        signature.identityValidated(),
        signature.typedSignature().orElse(null));
  }
}
