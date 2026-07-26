package dev.civicpulse.identity.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import dev.civicpulse.identity.application.port.in.ProvisionSyncedAccountUseCase.ProvisionSyncedAccountCommand;
import dev.civicpulse.identity.application.port.out.AccountRepository;
import dev.civicpulse.identity.application.port.out.DocumentCipher;
import dev.civicpulse.identity.application.port.out.EventPublisher;
import dev.civicpulse.identity.domain.model.Account;
import dev.civicpulse.identity.domain.model.AccountId;
import dev.civicpulse.identity.domain.model.AccountType;
import dev.civicpulse.identity.domain.model.DocumentType;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProvisionSyncedAccountServiceTest {

  private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");

  @Mock private AccountRepository accountRepository;
  @Mock private DocumentCipher documentCipher;
  @Mock private EventPublisher eventPublisher;

  private ProvisionSyncedAccountService service;

  @BeforeEach
  void setUp() {
    service = new ProvisionSyncedAccountService(accountRepository, documentCipher, eventPublisher, Clock.fixed(NOW, ZoneOffset.UTC));
  }

  @Test
  void updatesExistingAccountMatchedByExternalSourceAndExternalId() {
    Account existing =
        Account.registerSynced(
            AccountId.generate(), AccountType.PARTY, "Old Name", "solidariedade", "solidariedade@sync.gov.br", DocumentType.CNPJ,
            "cnpj-hash", new byte[] {1}, "http://old-logo", "CAMARA_PARTIDO", "36899", NOW);
    ProvisionSyncedAccountCommand command =
        new ProvisionSyncedAccountCommand(
            "New Name", "solidariedade", "solidariedade@sync.gov.br", "http://new-logo", DocumentType.CNPJ, "11222333000181", "CAMARA_PARTIDO", "36899");
    when(accountRepository.findByExternalSourceAndExternalId("CAMARA_PARTIDO", "36899")).thenReturn(Optional.of(existing));
    when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));

    Account result = service.provisionOrUpdate(AccountType.PARTY, command);

    assertThat(result.name()).isEqualTo("New Name");
    assertThat(result.avatarUrl()).contains("http://new-logo");
    verify(accountRepository, never()).findByEmail(any());
    verify(eventPublisher, never()).publish(any());
  }

  @Test
  void fallsBackToEmailMatchWhenADifferentSyncSourceAlreadyCreatedTheSameParty() {
    // The exact bug this guards: Câmara's federal sync creates a party account under
    // ("CAMARA_PARTIDO", "36899"); TSE's state/municipal sync later discovers the same party
    // under ("TSE_PARTIDO", "SOLIDARIEDADE") but derives the identical sync email — without the
    // email fallback this would 409 instead of recognizing the same account.
    Account existing =
        Account.registerSynced(
            AccountId.generate(), AccountType.PARTY, "Solidariedade", "solidariedade", "solidariedade@sync.gov.br", DocumentType.CNPJ,
            "cnpj-hash", new byte[] {1}, "http://camara-logo", "CAMARA_PARTIDO", "36899", NOW);
    ProvisionSyncedAccountCommand command =
        new ProvisionSyncedAccountCommand(
            "Solidariedade", "solidariedade", "solidariedade@sync.gov.br", null, DocumentType.CNPJ, "99988877000199", "TSE_PARTIDO", "SOLIDARIEDADE");
    when(accountRepository.findByExternalSourceAndExternalId("TSE_PARTIDO", "SOLIDARIEDADE")).thenReturn(Optional.empty());
    when(accountRepository.findByEmail("solidariedade@sync.gov.br")).thenReturn(Optional.of(existing));
    when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));

    Account result = service.provisionOrUpdate(AccountType.PARTY, command);

    assertThat(result.id()).isEqualTo(existing.id());
    assertThat(result.externalSource()).contains("CAMARA_PARTIDO"); // provenance untouched
    verify(accountRepository, never()).existsByEmail(any());
    verify(eventPublisher, never()).publish(any());
  }

  @Test
  void neverUsesEmailFallbackToTouchARealClaimedAccount() {
    Account claimed =
        Account.register(
            AccountId.generate(), AccountType.CITIZEN, "Real Person", "realperson", "solidariedade@sync.gov.br", "real-hash",
            DocumentType.CPF, "cpf-hash", new byte[] {1}, NOW);
    ProvisionSyncedAccountCommand command =
        new ProvisionSyncedAccountCommand(
            "Solidariedade", "solidariedade", "solidariedade@sync.gov.br", null, DocumentType.CNPJ, "99988877000199", "TSE_PARTIDO", "SOLIDARIEDADE");
    when(accountRepository.findByExternalSourceAndExternalId("TSE_PARTIDO", "SOLIDARIEDADE")).thenReturn(Optional.empty());
    when(accountRepository.findByEmail("solidariedade@sync.gov.br")).thenReturn(Optional.of(claimed));
    when(accountRepository.existsByEmail("solidariedade@sync.gov.br")).thenReturn(true);

    org.assertj.core.api.Assertions.assertThatThrownBy(() -> service.provisionOrUpdate(AccountType.PARTY, command))
        .isInstanceOf(dev.civicpulse.identity.domain.exception.DuplicateAccountException.class);

    verify(accountRepository, never()).save(any());
  }

  @Test
  void createsBrandNewSyncedAccountWhenNothingMatches() {
    ProvisionSyncedAccountCommand command =
        new ProvisionSyncedAccountCommand(
            "New Deputy", "new-deputy-dep-1", "dep1@camara.leg.br", "http://photo", DocumentType.CPF, "12345678901", "CAMARA_DEPUTADO", "1");
    when(accountRepository.findByExternalSourceAndExternalId("CAMARA_DEPUTADO", "1")).thenReturn(Optional.empty());
    when(accountRepository.findByEmail("dep1@camara.leg.br")).thenReturn(Optional.empty());
    when(accountRepository.existsByEmail("dep1@camara.leg.br")).thenReturn(false);
    when(accountRepository.existsByHandle("new-deputy-dep-1")).thenReturn(false);
    when(documentCipher.hash("12345678901")).thenReturn("cpf-hash");
    when(accountRepository.existsByDocumentNumberHash("cpf-hash")).thenReturn(false);
    when(documentCipher.encrypt("12345678901")).thenReturn(new byte[] {9});
    when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));

    Account result = service.provisionOrUpdate(AccountType.POLITICIAN, command);

    assertThat(result.isSynced()).isTrue();
    assertThat(result.passwordHash()).isEmpty();
    verify(eventPublisher).publish(any());
  }
}
