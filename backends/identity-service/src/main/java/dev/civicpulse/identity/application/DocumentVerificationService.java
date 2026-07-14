package dev.civicpulse.identity.application;

import dev.civicpulse.identity.application.port.in.VerifyDocumentUseCase;
import dev.civicpulse.identity.application.port.out.AccountRepository;
import dev.civicpulse.identity.application.port.out.DocumentVerificationAttemptRepository;
import dev.civicpulse.identity.application.port.out.DocumentVerificationGateway;
import dev.civicpulse.identity.application.port.out.EventPublisher;
import dev.civicpulse.identity.domain.event.AccountVerified;
import dev.civicpulse.identity.domain.exception.AccountNotFoundException;
import dev.civicpulse.identity.domain.model.Account;
import dev.civicpulse.identity.domain.model.AccountId;
import dev.civicpulse.identity.domain.model.DocumentStatus;
import dev.civicpulse.identity.domain.model.DocumentVerificationAttempt;
import java.time.Clock;
import java.time.Instant;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Application-layer orchestration in front of the {@link DocumentVerificationGateway}
 * anti-corruption layer — the gateway itself doesn't know about accounts or events, this
 * service is what ties a raw provider response back to the domain. */
@Service
public class DocumentVerificationService implements VerifyDocumentUseCase {

  private final AccountRepository accountRepository;
  private final DocumentVerificationAttemptRepository attemptRepository;
  private final DocumentVerificationGateway gateway;
  private final EventPublisher eventPublisher;
  private final Clock clock;

  public DocumentVerificationService(
      AccountRepository accountRepository,
      DocumentVerificationAttemptRepository attemptRepository,
      DocumentVerificationGateway gateway,
      EventPublisher eventPublisher,
      Clock clock) {
    this.accountRepository = accountRepository;
    this.attemptRepository = attemptRepository;
    this.gateway = gateway;
    this.eventPublisher = eventPublisher;
    this.clock = clock;
  }

  @Override
  @Transactional
  public DocumentVerificationAttempt verify(AccountId accountId) {
    Account account = accountRepository.findById(accountId).orElseThrow(() -> new AccountNotFoundException(accountId.toString()));
    var documentType = account.documentType().orElseThrow(() -> new IllegalStateException("Account has no document to verify"));

    // The raw document number is never re-read from storage in plaintext for this call — a
    // real provider integration would receive it at registration time and reference this
    // account by id/hash for the callback, not by asking us to decrypt and resend it.
    DocumentVerificationGateway.Result result = gateway.verify(documentType, account.documentNumberHash().orElse(""));

    Instant now = clock.instant();
    DocumentVerificationAttempt attempt =
        DocumentVerificationAttempt.record(accountId, documentType, result.status(), result.providerName(), result.providerRef(), now);
    DocumentVerificationAttempt saved = attemptRepository.save(attempt);

    if (result.status() == DocumentStatus.VERIFIED) {
      account.markVerified(now);
      accountRepository.save(account);
      eventPublisher.publish(new AccountVerified(accountId.value(), now));
    }

    return saved;
  }
}
