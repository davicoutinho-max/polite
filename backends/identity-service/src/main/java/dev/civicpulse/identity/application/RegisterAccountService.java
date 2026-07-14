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
    if (accountRepository.existsByEmail(command.email())) {
      throw new DuplicateAccountException("email");
    }
    if (accountRepository.existsByHandle(command.handle())) {
      throw new DuplicateAccountException("handle");
    }

    String documentNumberHash = null;
    byte[] documentNumberEncrypted = null;
    DocumentType documentType = command.documentType();

    if (accountType != AccountType.ADMIN) {
      // Mirrors the frontend's br-documents.ts rule exactly: a digit-count check, not a full
      // checksum — see InvalidDocumentNumberException's javadoc.
      String digitsOnly = command.rawDocumentNumber() == null ? "" : command.rawDocumentNumber().replaceAll("\\D", "");
      if (documentType == null || digitsOnly.length() != documentType.digitCount()) {
        throw new InvalidDocumentNumberException(documentType == null ? DocumentType.CPF : documentType);
      }
      documentNumberHash = documentCipher.hash(digitsOnly);
      if (accountRepository.existsByDocumentNumberHash(documentNumberHash)) {
        throw new DuplicateAccountException(documentType.code().toUpperCase());
      }
      documentNumberEncrypted = documentCipher.encrypt(digitsOnly);
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
