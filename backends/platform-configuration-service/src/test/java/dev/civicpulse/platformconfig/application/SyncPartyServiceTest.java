package dev.civicpulse.platformconfig.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import dev.civicpulse.platformconfig.application.port.in.SyncPartyUseCase.SyncPartyCommand;
import dev.civicpulse.platformconfig.application.port.out.EventPublisher;
import dev.civicpulse.platformconfig.application.port.out.IdentityProvisioningGateway;
import dev.civicpulse.platformconfig.application.port.out.IdentityProvisioningGateway.ProvisionedAccount;
import dev.civicpulse.platformconfig.application.port.out.PartyRegistryRepository;
import dev.civicpulse.platformconfig.domain.model.PartyRegistryEntry;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SyncPartyServiceTest {

  private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");

  @Mock private PartyRegistryRepository partyRegistryRepository;
  @Mock private IdentityProvisioningGateway identityProvisioningGateway;
  @Mock private EventPublisher eventPublisher;

  private SyncPartyService service;

  @BeforeEach
  void setUp() {
    service = new SyncPartyService(partyRegistryRepository, identityProvisioningGateway, eventPublisher, Clock.fixed(NOW, ZoneOffset.UTC));
  }

  @Test
  void stripsSpacesAndPunctuationFromAcronymWhenBuildingHandleAndEmail() {
    // A live bug: TSE's SG_PARTIDO for some coligação-context rows renders as "PC do B" (spaces),
    // which a plain toLowerCase() bakes straight into an invalid handle/email and gets rejected
    // by identity-service's validation — the registry's own acronym must stay untouched though.
    UUID accountId = UUID.randomUUID();
    SyncPartyCommand command = new SyncPartyCommand("Partido Comunista do Brasil", "PC do B", 65, null, "cnpj", "11222333000181", "TSE_PARTIDO", "PC do B");
    when(partyRegistryRepository.findByAcronym("PC do B")).thenReturn(Optional.empty());
    when(identityProvisioningGateway.provisionSyncedPartyAccount(
            eq("Partido Comunista do Brasil"), eq("pcdob"), eq("pcdob@sync.gov.br"), any(), any(), any(), any(), any()))
        .thenReturn(new ProvisionedAccount(accountId, "Partido Comunista do Brasil", "pcdob"));
    when(partyRegistryRepository.save(any(PartyRegistryEntry.class))).thenAnswer(invocation -> invocation.getArgument(0));

    PartyRegistryEntry result = service.syncParty(command);

    assertThat(result.acronym()).isEqualTo("PC do B");
    verify(identityProvisioningGateway)
        .provisionSyncedPartyAccount(any(), eq("pcdob"), eq("pcdob@sync.gov.br"), any(), any(), any(), any(), any());
  }

  @Test
  void refreshingAnAlreadySyncedPartyAlsoUsesTheSanitizedAcronym() {
    UUID accountId = UUID.randomUUID();
    PartyRegistryEntry existing = PartyRegistryEntry.register(accountId, "Partido Comunista do Brasil", "PC do B", 65, null, null, NOW);
    SyncPartyCommand command = new SyncPartyCommand("Partido Comunista do Brasil", "PC do B", 65, "http://logo", "cnpj", "11222333000181", "TSE_PARTIDO", "PC do B");
    when(partyRegistryRepository.findByAcronym("PC do B")).thenReturn(Optional.of(existing));

    PartyRegistryEntry result = service.syncParty(command);

    assertThat(result).isSameAs(existing);
    verify(identityProvisioningGateway)
        .provisionSyncedPartyAccount(any(), eq("pcdob"), eq("pcdob@sync.gov.br"), any(), any(), any(), any(), any());
  }
}
