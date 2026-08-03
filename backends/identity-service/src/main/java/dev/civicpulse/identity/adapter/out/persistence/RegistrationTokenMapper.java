package dev.civicpulse.identity.adapter.out.persistence;

import dev.civicpulse.identity.domain.model.AccountType;
import dev.civicpulse.identity.domain.model.RegistrationToken;
import org.springframework.stereotype.Component;

@Component
class RegistrationTokenMapper {

  RegistrationToken toDomain(RegistrationTokenJpaEntity entity) {
    return RegistrationToken.reconstitute(
        entity.getId(),
        entity.getToken(),
        AccountType.fromCode(entity.getAccountType()),
        entity.getIssuedByAccountId(),
        entity.getTargetEmail(),
        entity.getPrefillDataJson(),
        entity.getCreatedAt(),
        entity.getExpiresAt(),
        entity.getConsumedAt(),
        entity.isInvalidated());
  }

  RegistrationTokenJpaEntity toEntity(RegistrationToken token) {
    return new RegistrationTokenJpaEntity(
        token.id(),
        token.token(),
        token.accountType().code(),
        token.issuedByAccountId(),
        token.targetEmail().orElse(null),
        token.prefillDataJson().orElse(null),
        token.createdAt(),
        token.expiresAt(),
        token.consumedAt().orElse(null),
        token.invalidated());
  }
}
