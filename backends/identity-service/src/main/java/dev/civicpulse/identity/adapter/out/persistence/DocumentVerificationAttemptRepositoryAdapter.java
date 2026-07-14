package dev.civicpulse.identity.adapter.out.persistence;

import dev.civicpulse.identity.application.port.out.DocumentVerificationAttemptRepository;
import dev.civicpulse.identity.domain.model.AccountId;
import dev.civicpulse.identity.domain.model.DocumentVerificationAttempt;
import org.springframework.stereotype.Component;

@Component
class DocumentVerificationAttemptRepositoryAdapter implements DocumentVerificationAttemptRepository {

  private final DocumentVerificationAttemptJpaRepository jpaRepository;

  DocumentVerificationAttemptRepositoryAdapter(DocumentVerificationAttemptJpaRepository jpaRepository) {
    this.jpaRepository = jpaRepository;
  }

  @Override
  public DocumentVerificationAttempt save(DocumentVerificationAttempt attempt) {
    var saved =
        jpaRepository.save(
            new DocumentVerificationAttemptJpaEntity(
                attempt.id(),
                attempt.accountId().value(),
                attempt.documentType(),
                attempt.status(),
                attempt.provider(),
                attempt.providerRef().orElse(null),
                attempt.checkedAt()));
    return DocumentVerificationAttempt.reconstitute(
        saved.getId(),
        AccountId.of(saved.getAccountId()),
        saved.getDocumentType(),
        saved.getStatus(),
        saved.getProvider(),
        saved.getProviderRef(),
        saved.getCheckedAt());
  }
}
