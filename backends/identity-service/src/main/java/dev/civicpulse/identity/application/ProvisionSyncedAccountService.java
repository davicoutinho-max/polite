package dev.civicpulse.identity.application;

import dev.civicpulse.identity.application.port.in.ProvisionSyncedAccountUseCase;
import dev.civicpulse.identity.application.port.out.AccountRepository;
import dev.civicpulse.identity.application.port.out.DocumentCipher;
import dev.civicpulse.identity.application.port.out.EventPublisher;
import dev.civicpulse.identity.domain.event.AccountRegistered;
import dev.civicpulse.identity.domain.exception.DuplicateAccountException;
import dev.civicpulse.identity.domain.exception.InvalidDocumentNumberException;
import dev.civicpulse.identity.domain.model.Account;
import dev.civicpulse.identity.domain.model.AccountId;
import dev.civicpulse.identity.domain.model.AccountType;
import dev.civicpulse.identity.domain.model.DocumentType;
import java.time.Clock;
import java.time.Instant;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProvisionSyncedAccountService implements ProvisionSyncedAccountUseCase {

  private final AccountRepository accountRepository;
  private final DocumentCipher documentCipher;
  private final EventPublisher eventPublisher;
  private final Clock clock;

  public ProvisionSyncedAccountService(
      AccountRepository accountRepository, DocumentCipher documentCipher, EventPublisher eventPublisher, Clock clock) {
    this.accountRepository = accountRepository;
    this.documentCipher = documentCipher;
    this.eventPublisher = eventPublisher;
    this.clock = clock;
  }

  @Override
  @Transactional
  public Account provisionOrUpdate(AccountType accountType, ProvisionSyncedAccountCommand command) {
    Instant now = clock.instant();

    // Two different sync sources can legitimately discover the same real-world party/politician
    // under different (externalSource, externalId) pairs — e.g. a party seen both via Câmara's
    // federal sync ("CAMARA_PARTIDO"/its Câmara id) and via TSE's state/municipal sync
    // ("TSE_PARTIDO"/its acronym). Both derive the same deterministic sync email from the party's
    // acronym (see platform-configuration-service's SyncPartyService), so falling back to an
    // email match — restricted to accounts that are still synced, never a real claimed one — is
    // what keeps the second source from colliding with the first instead of recognizing it.
    var existing = accountRepository.findByExternalSourceAndExternalId(command.externalSource(), command.externalId());
    if (existing.isEmpty()) {
      existing = accountRepository.findByEmail(command.email()).filter(Account::isSynced);
    }
    if (existing.isPresent()) {
      Account account = existing.get();
      account.updateSyncedProfile(command.name(), command.avatarUrl(), now);
      return accountRepository.save(account);
    }

    if (accountRepository.existsByEmail(command.email())) {
      throw new DuplicateAccountException("email");
    }
    if (accountRepository.existsByHandle(command.handle())) {
      throw new DuplicateAccountException("handle");
    }

    DocumentType documentType = command.documentType();
    String digitsOnly = command.rawDocumentNumber() == null ? "" : command.rawDocumentNumber().replaceAll("\\D", "");
    if (documentType == null || digitsOnly.length() != documentType.digitCount()) {
      throw new InvalidDocumentNumberException(documentType == null ? DocumentType.CPF : documentType);
    }
    String documentNumberHash = documentCipher.hash(digitsOnly);
    if (accountRepository.existsByDocumentNumberHash(documentNumberHash)) {
      throw new DuplicateAccountException(documentType.code().toUpperCase());
    }
    byte[] documentNumberEncrypted = documentCipher.encrypt(digitsOnly);

    Account account =
        Account.registerSynced(
            AccountId.generate(),
            accountType,
            command.name(),
            command.handle(),
            command.email(),
            documentType,
            documentNumberHash,
            documentNumberEncrypted,
            command.avatarUrl(),
            command.externalSource(),
            command.externalId(),
            now);

    Account saved = accountRepository.save(account);

    eventPublisher.publish(
        new AccountRegistered(saved.id().value(), saved.accountType().code(), saved.documentNumberHash().orElse(null), now));

    return saved;
  }
}
