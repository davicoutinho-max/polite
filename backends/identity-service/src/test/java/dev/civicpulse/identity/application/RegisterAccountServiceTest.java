package dev.civicpulse.identity.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import dev.civicpulse.identity.application.port.in.RegisterAccountUseCase.RegisterAccountCommand;
import dev.civicpulse.identity.application.port.out.AccountRepository;
import dev.civicpulse.identity.application.port.out.DocumentCipher;
import dev.civicpulse.identity.application.port.out.EventPublisher;
import dev.civicpulse.identity.application.port.out.PasswordHasher;
import dev.civicpulse.identity.domain.event.AccountRegistered;
import dev.civicpulse.identity.domain.exception.AccountNotFoundException;
import dev.civicpulse.identity.domain.exception.DuplicateAccountException;
import dev.civicpulse.identity.domain.exception.InvalidDocumentNumberException;
import dev.civicpulse.identity.domain.model.Account;
import dev.civicpulse.identity.domain.model.AccountId;
import dev.civicpulse.identity.domain.model.AccountType;
import dev.civicpulse.identity.domain.model.DocumentType;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RegisterAccountServiceTest {

  private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");

  @Mock private AccountRepository accountRepository;
  @Mock private PasswordHasher passwordHasher;
  @Mock private DocumentCipher documentCipher;
  @Mock private EventPublisher eventPublisher;

  private RegisterAccountService service;

  @BeforeEach
  void setUp() {
    Clock fixedClock = Clock.fixed(NOW, ZoneOffset.UTC);
    service =
        new RegisterAccountService(accountRepository, passwordHasher, documentCipher, eventPublisher, fixedClock);
  }

  @Test
  void registersCitizenAndPublishesAccountRegisteredEvent() {
    RegisterAccountCommand command =
        new RegisterAccountCommand("Jane Doe", "janedoe", "jane@example.com", "s3cret!", DocumentType.CPF, "529.982.247-25");

    when(documentCipher.hash("52998224725")).thenReturn("cpf-hash");
    when(accountRepository.findByDocumentNumberHash("cpf-hash")).thenReturn(java.util.Optional.empty());
    when(accountRepository.existsByEmail("jane@example.com")).thenReturn(false);
    when(accountRepository.existsByHandle("janedoe")).thenReturn(false);
    when(documentCipher.encrypt("52998224725")).thenReturn(new byte[] {9, 9, 9});
    when(passwordHasher.hash("s3cret!")).thenReturn("hashed-password");
    when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));

    Account result = service.registerCitizen(command);

    assertThat(result.accountType()).isEqualTo(AccountType.CITIZEN);
    assertThat(result.documentNumberHash()).contains("cpf-hash");
    assertThat(result.passwordHash()).isEqualTo("hashed-password");

    var eventCaptor = org.mockito.ArgumentCaptor.forClass(AccountRegistered.class);
    verify(eventPublisher).publish(eventCaptor.capture());
    assertThat(eventCaptor.getValue().accountType()).isEqualTo("citizen");
    assertThat(eventCaptor.getValue().documentHash()).isEqualTo("cpf-hash");
  }

  @Test
  void rejectsDuplicateEmailWhenNoSyncedProfileToClaimExists() {
    RegisterAccountCommand command =
        new RegisterAccountCommand("Jane Doe", "janedoe", "jane@example.com", "s3cret!", DocumentType.CPF, "529.982.247-25");
    when(documentCipher.hash("52998224725")).thenReturn("cpf-hash");
    when(accountRepository.findByDocumentNumberHash("cpf-hash")).thenReturn(java.util.Optional.empty());
    when(accountRepository.existsByEmail("jane@example.com")).thenReturn(true);

    assertThatThrownBy(() -> service.registerCitizen(command))
        .isInstanceOf(DuplicateAccountException.class)
        .satisfies(ex -> assertThat(((DuplicateAccountException) ex).field()).isEqualTo("email"));

    verifyNoInteractions(passwordHasher, eventPublisher);
  }

  @Test
  void rejectsDuplicateHandle() {
    RegisterAccountCommand command =
        new RegisterAccountCommand("Jane Doe", "janedoe", "jane@example.com", "s3cret!", DocumentType.CPF, "529.982.247-25");
    when(documentCipher.hash("52998224725")).thenReturn("cpf-hash");
    when(accountRepository.findByDocumentNumberHash("cpf-hash")).thenReturn(java.util.Optional.empty());
    when(accountRepository.existsByEmail(anyString())).thenReturn(false);
    when(accountRepository.existsByHandle("janedoe")).thenReturn(true);

    assertThatThrownBy(() -> service.registerCitizen(command))
        .isInstanceOf(DuplicateAccountException.class)
        .satisfies(ex -> assertThat(((DuplicateAccountException) ex).field()).isEqualTo("handle"));
  }

  @Test
  void rejectsCpfWithWrongDigitCount() {
    RegisterAccountCommand command =
        new RegisterAccountCommand("Jane Doe", "janedoe", "jane@example.com", "s3cret!", DocumentType.CPF, "123");

    assertThatThrownBy(() -> service.registerCitizen(command)).isInstanceOf(InvalidDocumentNumberException.class);

    verifyNoInteractions(accountRepository, passwordHasher, eventPublisher);
  }

  @Test
  void rejectsCpfWithInvalidCheckDigit() {
    // Right digit count (11), but not a real CPF — the check digits don't match. A direct API
    // caller bypassing the frontend's own isValidCpf check must still be rejected here.
    RegisterAccountCommand command =
        new RegisterAccountCommand("Jane Doe", "janedoe", "jane@example.com", "s3cret!", DocumentType.CPF, "123.456.789-00");

    assertThatThrownBy(() -> service.registerCitizen(command)).isInstanceOf(InvalidDocumentNumberException.class);

    verifyNoInteractions(accountRepository, passwordHasher, eventPublisher);
  }

  @Test
  void rejectsDuplicateDocumentNumberHashWhenExistingAccountIsNotSynced() {
    RegisterAccountCommand command =
        new RegisterAccountCommand("Jane Doe", "janedoe", "jane@example.com", "s3cret!", DocumentType.CPF, "529.982.247-25");
    Account existing =
        Account.register(
            dev.civicpulse.identity.domain.model.AccountId.generate(),
            AccountType.CITIZEN,
            "Existing Owner",
            "existing-owner",
            "existing@example.com",
            "some-hash",
            DocumentType.CPF,
            "cpf-hash",
            new byte[] {1},
            NOW);
    when(documentCipher.hash("52998224725")).thenReturn("cpf-hash");
    when(accountRepository.findByDocumentNumberHash("cpf-hash")).thenReturn(java.util.Optional.of(existing));

    assertThatThrownBy(() -> service.registerCitizen(command))
        .isInstanceOf(DuplicateAccountException.class)
        .satisfies(ex -> assertThat(((DuplicateAccountException) ex).field()).isEqualTo("CPF"));

    verifyNoInteractions(passwordHasher, eventPublisher);
  }

  @Test
  void claimsExistingSyncedAccountInsteadOfCreatingADuplicate() {
    RegisterAccountCommand command =
        new RegisterAccountCommand(
            "Acácio Favacho", "acacio-favacho", "acacio@example.com", "s3cret!", DocumentType.CPF, "529.982.247-25");
    Account synced =
        Account.registerSynced(
            dev.civicpulse.identity.domain.model.AccountId.generate(),
            AccountType.POLITICIAN,
            "Acácio Favacho",
            "acacio-favacho-dep-204379",
            "dep.acaciofavacho@camara.leg.br",
            DocumentType.CPF,
            "cpf-hash",
            new byte[] {1},
            "http://photo",
            "CAMARA_DEPUTADO",
            "204379",
            NOW);
    when(documentCipher.hash("52998224725")).thenReturn("cpf-hash");
    when(accountRepository.findByDocumentNumberHash("cpf-hash")).thenReturn(java.util.Optional.of(synced));
    when(passwordHasher.hash("s3cret!")).thenReturn("hashed-password");
    when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));

    Account result = service.provisionAccount(AccountType.POLITICIAN, command);

    assertThat(result.id()).isEqualTo(synced.id());
    assertThat(result.isSynced()).isFalse();
    assertThat(result.externalSource()).isEmpty();
    assertThat(result.passwordHash()).isEqualTo("hashed-password");
    // The government-sourced identity wins, not whatever the claimer typed — see Account.claim.
    assertThat(result.email()).isEqualTo("dep.acaciofavacho@camara.leg.br");
    assertThat(result.handle()).isEqualTo("acacio-favacho-dep-204379");

    verifyNoInteractions(eventPublisher);
    verify(accountRepository, never()).existsByEmail(anyString());
    verify(accountRepository, never()).existsByHandle(anyString());
  }

  @Test
  void provisionsAdminAccountWithoutDocumentData() {
    RegisterAccountCommand command =
        new RegisterAccountCommand("Platform Admin", "admin", "admin@example.com", "s3cret!", null, null);
    when(accountRepository.existsByEmail(anyString())).thenReturn(false);
    when(accountRepository.existsByHandle(anyString())).thenReturn(false);
    when(passwordHasher.hash("s3cret!")).thenReturn("hashed-password");
    when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));

    Account result = service.provisionAccount(AccountType.ADMIN, command);

    assertThat(result.accountType()).isEqualTo(AccountType.ADMIN);
    assertThat(result.documentType()).isEmpty();
    verifyNoInteractions(documentCipher);
  }

  @Test
  void claimsSpecificAccountByIdRegardlessOfDocumentHashMismatch() {
    // The synced profile carries a synthetic document number (a TSE-sourced state/municipal
    // politician, say) — nothing like the citizen's own real CPF typed here. The explicit
    // claimAccountId (chosen via directory search) must still win.
    AccountId targetId = AccountId.generate();
    Account synced =
        Account.registerSynced(
            targetId,
            AccountType.POLITICIAN,
            "Maria Souza",
            "maria-souza-ver-1234",
            "ver1234@sync.gov.br",
            DocumentType.CPF,
            "synthetic-hash",
            new byte[] {0},
            "http://photo",
            "TSE_CANDIDATO",
            "1234",
            NOW);
    RegisterAccountCommand command =
        new RegisterAccountCommand("Maria Souza", "mariasouza", "maria@example.com", "s3cret!", DocumentType.CPF, "529.982.247-25");

    when(accountRepository.findById(targetId)).thenReturn(java.util.Optional.of(synced));
    when(documentCipher.hash("52998224725")).thenReturn("real-cpf-hash");
    when(accountRepository.existsByDocumentNumberHash("real-cpf-hash")).thenReturn(false);
    when(documentCipher.encrypt("52998224725")).thenReturn(new byte[] {9});
    when(passwordHasher.hash("s3cret!")).thenReturn("hashed-password");
    when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));

    Account result = service.registerCitizen(command, targetId);

    assertThat(result.id()).isEqualTo(targetId);
    assertThat(result.isSynced()).isFalse();
    assertThat(result.documentNumberHash()).contains("real-cpf-hash");
    assertThat(result.passwordHash()).isEqualTo("hashed-password");
    verify(eventPublisher).publish(any(AccountRegistered.class));
  }

  @Test
  void rejectsClaimOfAnAlreadyClaimedAccount() {
    AccountId targetId = AccountId.generate();
    Account alreadyClaimed =
        Account.register(
            targetId, AccountType.POLITICIAN, "Maria Souza", "mariasouza", "maria@example.com", "hash", DocumentType.CPF, "hash", new byte[] {1}, NOW);
    RegisterAccountCommand command =
        new RegisterAccountCommand("Maria Souza", "mariasouza2", "maria2@example.com", "s3cret!", DocumentType.CPF, "529.982.247-25");

    when(accountRepository.findById(targetId)).thenReturn(java.util.Optional.of(alreadyClaimed));

    assertThatThrownBy(() -> service.registerCitizen(command, targetId)).isInstanceOf(DuplicateAccountException.class);

    verifyNoInteractions(passwordHasher, eventPublisher);
  }

  @Test
  void rejectsClaimOfAnUnknownAccountId() {
    AccountId targetId = AccountId.generate();
    RegisterAccountCommand command =
        new RegisterAccountCommand("Maria Souza", "mariasouza", "maria@example.com", "s3cret!", DocumentType.CPF, "529.982.247-25");

    when(accountRepository.findById(targetId)).thenReturn(java.util.Optional.empty());

    assertThatThrownBy(() -> service.registerCitizen(command, targetId)).isInstanceOf(AccountNotFoundException.class);
  }

  @Test
  void checkDocumentFindsAnUnclaimedSyncedProfile() {
    Account synced =
        Account.registerSynced(
            AccountId.generate(),
            AccountType.POLITICIAN,
            "Acácio Favacho",
            "acacio-favacho-dep-204379",
            "dep.acaciofavacho@camara.leg.br",
            DocumentType.CPF,
            "cpf-hash",
            new byte[] {1},
            "http://photo",
            "CAMARA_DEPUTADO",
            "204379",
            NOW);
    when(documentCipher.hash("52998224725")).thenReturn("cpf-hash");
    when(accountRepository.findByDocumentNumberHash("cpf-hash")).thenReturn(java.util.Optional.of(synced));

    var result = service.checkDocument("529.982.247-25");

    assertThat(result).isPresent();
    assertThat(result.get().name()).isEqualTo("Acácio Favacho");
    assertThat(result.get().accountType()).isEqualTo("politician");
  }

  @Test
  void checkDocumentReturnsEmptyWhenNoMatchOrAlreadyClaimed() {
    when(documentCipher.hash("52998224725")).thenReturn("cpf-hash");
    when(accountRepository.findByDocumentNumberHash("cpf-hash")).thenReturn(java.util.Optional.empty());

    assertThat(service.checkDocument("529.982.247-25")).isEmpty();
  }
}
