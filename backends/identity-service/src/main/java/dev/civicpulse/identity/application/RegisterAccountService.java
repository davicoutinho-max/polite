package dev.civicpulse.identity.application;

import dev.civicpulse.identity.application.port.in.RegisterAccountUseCase;
import dev.civicpulse.identity.application.port.out.AccountRepository;
import dev.civicpulse.identity.application.port.out.DocumentCipher;
import dev.civicpulse.identity.application.port.out.EventPublisher;
import dev.civicpulse.identity.application.port.out.PasswordHasher;
import dev.civicpulse.identity.domain.event.AccountRegistered;
import dev.civicpulse.identity.domain.exception.DuplicateAccountException;
import dev.civicpulse.identity.domain.exception.InvalidDocumentNumberException;
import dev.civicpulse.identity.domain.model.Account;
import dev.civicpulse.identity.domain.model.AccountId;
import dev.civicpulse.identity.domain.model.AccountType;
import dev.civicpulse.identity.domain.model.DocumentNumberValidator;
import dev.civicpulse.identity.domain.model.DocumentType;
import java.time.Clock;
import java.time.Instant;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RegisterAccountService implements RegisterAccountUseCase {

  private final AccountRepository accountRepository;
  private final PasswordHasher passwordHasher;
  private final DocumentCipher documentCipher;
  private final EventPublisher eventPublisher;
  private final Clock clock;

  public RegisterAccountService(
      AccountRepository accountRepository,
      PasswordHasher passwordHasher,
      DocumentCipher documentCipher,
      EventPublisher eventPublisher,
      Clock clock) {
    this.accountRepository = accountRepository;
    this.passwordHasher = passwordHasher;
    this.documentCipher = documentCipher;
    this.eventPublisher = eventPublisher;
    this.clock = clock;
  }

  @Override
  @Transactional
  public Account registerCitizen(RegisterAccountCommand command) {
    return provisionAccount(AccountType.CITIZEN, command);
  }

  @Override
  @Transactional
  public Account provisionAccount(AccountType accountType, RegisterAccountCommand command) {
    DocumentType documentType = command.documentType();
    String documentNumberHash = null;
    byte[] documentNumberEncrypted = null;

    if (accountType != AccountType.ADMIN) {
      // Mirrors the frontend's br-documents.ts check-digit validation exactly (isValidCpf/
      // isValidCnpj there) — enforced server-side too since a direct API caller never goes
      // through the frontend's own check. See InvalidDocumentNumberException's javadoc.
      String digitsOnly = command.rawDocumentNumber() == null ? "" : command.rawDocumentNumber().replaceAll("\\D", "");
      if (documentType == null || !DocumentNumberValidator.isValid(documentType, digitsOnly)) {
        throw new InvalidDocumentNumberException(documentType == null ? DocumentType.CPF : documentType);
      }
      documentNumberHash = documentCipher.hash(digitsOnly);

      // Checked before email/handle uniqueness on purpose: a real politician/party registering
      // with the same CPF/CNPJ a government-data sync already used to build an unclaimed profile
      // (see Account.registerSynced) must claim that profile, not get rejected by its own
      // synced email/handle already being "taken" — see Account.claim's javadoc.
      var existingByDocument = accountRepository.findByDocumentNumberHash(documentNumberHash);
      if (existingByDocument.isPresent()) {
        Account existing = existingByDocument.get();
        if (!existing.isSynced()) {
          throw new DuplicateAccountException(documentType.code().toUpperCase());
        }
        existing.claim(passwordHasher.hash(command.rawPassword()), clock.instant());
        return accountRepository.save(existing);
      }

      documentNumberEncrypted = documentCipher.encrypt(digitsOnly);
    }

    if (accountRepository.existsByEmail(command.email())) {
      throw new DuplicateAccountException("email");
    }
    if (accountRepository.existsByHandle(command.handle())) {
      throw new DuplicateAccountException("handle");
    }

    Instant now = clock.instant();
    Account account =
        Account.register(
            AccountId.generate(),
            accountType,
            command.name(),
            command.handle(),
            command.email(),
            passwordHasher.hash(command.rawPassword()),
            documentType,
            documentNumberHash,
            documentNumberEncrypted,
            now);

    Account saved = accountRepository.save(account);

    eventPublisher.publish(
        new AccountRegistered(
            saved.id().value(),
            saved.accountType().code(),
            saved.documentNumberHash().orElse(null),
            now));

    return saved;
  }
}
