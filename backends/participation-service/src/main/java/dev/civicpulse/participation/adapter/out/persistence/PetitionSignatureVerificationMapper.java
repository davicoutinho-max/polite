package dev.civicpulse.participation.adapter.out.persistence;

import dev.civicpulse.participation.domain.model.PendingSignatureVerification;
import org.springframework.stereotype.Component;

@Component
class PetitionSignatureVerificationMapper {

  PendingSignatureVerification toDomain(PetitionSignatureVerificationJpaEntity entity) {
    return PendingSignatureVerification.reconstitute(
        entity.getId(),
        entity.getPetitionId(),
        entity.getCitizenAccountId(),
        entity.getCode(),
        entity.getContact(),
        entity.getMethod(),
        entity.getFullName(),
        entity.getCpf(),
        entity.getBirthDate(),
        entity.getCity(),
        entity.getState(),
        entity.getElectoralData(),
        entity.isESignatureConsent(),
        entity.getTypedSignature(),
        entity.getExpiresAt(),
        entity.isConsumed());
  }

  PetitionSignatureVerificationJpaEntity toEntity(PendingSignatureVerification verification) {
    return new PetitionSignatureVerificationJpaEntity(
        verification.id(),
        verification.petitionId(),
        verification.citizenAccountId(),
        verification.code(),
        verification.contact().orElse(null),
        verification.method(),
        verification.fullName(),
        verification.cpf(),
        verification.birthDate().orElse(null),
        verification.city().orElse(null),
        verification.state().orElse(null),
        verification.electoralData().orElse(null),
        verification.eSignatureConsent(),
        verification.typedSignature(),
        verification.expiresAt(),
        verification.isConsumed());
  }
}
