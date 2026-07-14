package dev.civicpulse.privacycompliance.adapter.out.persistence;

import dev.civicpulse.privacycompliance.domain.model.AccountDeletionRequest;
import org.springframework.stereotype.Component;

@Component
class AccountDeletionRequestMapper {

  AccountDeletionRequest toDomain(AccountDeletionRequestJpaEntity entity) {
    return AccountDeletionRequest.reconstitute(
        entity.getId(), entity.getAccountId(), entity.getStatus(), entity.getRequestedAt(), entity.getCompletedAt());
  }

  AccountDeletionRequestJpaEntity toEntity(AccountDeletionRequest request) {
    return new AccountDeletionRequestJpaEntity(
        request.id(), request.accountId(), request.status(), request.requestedAt(), request.completedAt().orElse(null));
  }
}
