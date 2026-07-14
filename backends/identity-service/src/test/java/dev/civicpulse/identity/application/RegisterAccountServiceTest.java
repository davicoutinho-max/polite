package dev.civicpulse.identity.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import dev.civicpulse.identity.application.port.in.RegisterAccountUseCase.RegisterAccountCommand;
import dev.civicpulse.identity.application.port.out.AccountRepository;
import dev.civicpulse.identity.application.port.out.DocumentCipher;
import dev.civicpulse.identity.application.port.out.EventPublisher;
import dev.civicpulse.identity.application.port.out.PasswordHasher;
import dev.civicpulse.identity.domain.event.AccountRegistered;
import dev.civicpulse.identity.domain.exception.DuplicateAccountException;
import dev.civicpulse.identity.domain.exception.InvalidDocumentNumberException;
import dev.civicpulse.identity.domain.model.Account;
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
        new RegisterAccountCommand("Jane Doe", "janedoe", "jane@example.com", "s3cret!", DocumentType.CPF, "123.456.789-01");

    when(accountRepository.existsByEmail("jane@example.com")).thenReturn(false);
    when(accountRepository.existsByHandle("janedoe")).thenReturn(false);
    when(documentCipher.hash("12345678901")).thenReturn("cpf-hash");
    when(accountRepository.existsByDocumentNumberHash("cpf-hash")).thenReturn(false);
    when(documentCipher.encrypt("12345678901")).thenReturn(new byte[] {9, 9, 9});
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
  void rejectsDuplicateEmailBeforeTouchingDocumentCipher() {
    RegisterAccountCommand command =
        new RegisterAccountCommand("Jane Doe", "janedoe", "jane@example.com", "s3cret!", DocumentType.CPF, "123.456.789-01");
    when(accountRepository.existsByEmail("jane@example.com")).thenReturn(true);

    assertThatThrownBy(() -> service.registerCitizen(command))
        .isInstanceOf(DuplicateAccountException.class)
        .satisfies(ex -> assertThat(((DuplicateAccountException) ex).field()).isEqualTo("email"));

    verifyNoInteractions(documentCipher, passwordHasher, eventPublisher);
  }

  @Test
  void rejectsDuplicateHandle() {
    RegisterAccountCommand command =
        new RegisterAccountCommand("Jane Doe", "janedoe", "jane@example.com", "s3cret!", DocumentType.CPF, "123.456.789-01");
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
    when(accountRepository.existsByEmail(anyString())).thenReturn(false);
    when(accountRepository.existsByHandle(anyString())).thenReturn(false);

    assertThatThrownBy(() -> service.registerCitizen(command)).isInstanceOf(InvalidDocumentNumberException.class);

    verifyNoInteractions(passwordHasher, eventPublisher);
  }

  @Test
  void rejectsDuplicateDocumentNumberHash() {
    RegisterAccountCommand command =
        new RegisterAccountCommand("Jane Doe", "janedoe", "jane@example.com", "s3cret!", DocumentType.CPF, "123.456.789-01");
    when(accountRepository.existsByEmail(anyString())).thenReturn(false);
    when(accountRepository.existsByHandle(anyString())).thenReturn(false);
    when(documentCipher.hash("12345678901")).thenReturn("cpf-hash");
    when(accountRepository.existsByDocumentNumberHash("cpf-hash")).thenReturn(true);

    assertThatThrownBy(() -> service.registerCitizen(command))
        .isInstanceOf(DuplicateAccountException.class)
        .satisfies(ex -> assertThat(((DuplicateAccountException) ex).field()).isEqualTo("CPF"));
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
}
