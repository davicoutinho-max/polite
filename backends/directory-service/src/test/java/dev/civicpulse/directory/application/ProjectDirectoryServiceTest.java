package dev.civicpulse.directory.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import dev.civicpulse.directory.application.port.out.AccountLookupGateway;
import dev.civicpulse.directory.application.port.out.AccountLookupGateway.AccountSummary;
import dev.civicpulse.directory.application.port.out.PartyRepository;
import dev.civicpulse.directory.application.port.out.PoliticianRepository;
import dev.civicpulse.directory.domain.model.Party;
import dev.civicpulse.directory.domain.model.PartySpectrum;
import dev.civicpulse.directory.domain.model.Politician;
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
class ProjectDirectoryServiceTest {

  private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");

  @Mock private PoliticianRepository politicianRepository;
  @Mock private PartyRepository partyRepository;
  @Mock private AccountLookupGateway accountLookupGateway;

  private ProjectDirectoryService service;

  @BeforeEach
  void setUp() {
    service = new ProjectDirectoryService(politicianRepository, partyRepository, accountLookupGateway, Clock.fixed(NOW, ZoneOffset.UTC));
  }

  @Test
  void onAccountRegisteredProjectsPoliticianEnrichedViaIdentityLookup() {
    UUID accountId = UUID.randomUUID();
    when(politicianRepository.findById(accountId)).thenReturn(Optional.empty());
    when(accountLookupGateway.findAccount(accountId)).thenReturn(Optional.of(new AccountSummary(accountId, "Jane Doe", "janedoe", null)));

    service.onAccountRegistered(accountId, "politician");

    verify(politicianRepository).createIfAbsent(accountId, "Jane Doe", "janedoe", null, NOW);
  }

  @Test
  void onAccountRegisteredIgnoresNonPoliticianAccountTypes() {
    service.onAccountRegistered(UUID.randomUUID(), "citizen");

    verify(politicianRepository, never()).createIfAbsent(any(), any(), any(), any(), any());
  }

  @Test
  void onAccountRegisteredIsIdempotentIfAlreadyProjected() {
    UUID accountId = UUID.randomUUID();
    Politician existing = Politician.project(accountId, "Jane Doe", "janedoe", null, null, null, null, null, null, NOW);
    when(politicianRepository.findById(accountId)).thenReturn(Optional.of(existing));

    service.onAccountRegistered(accountId, "politician");

    verify(politicianRepository, never()).createIfAbsent(any(), any(), any(), any(), any());
    verify(accountLookupGateway, never()).findAccount(any());
  }

  @Test
  void onRepresentativeLinkedAssignsPartyAndOfficeViaTargetedUpdates() {
    UUID accountId = UUID.randomUUID();
    UUID partyId = UUID.randomUUID();
    Politician existing = Politician.project(accountId, "Jane Doe", "janedoe", null, null, null, null, null, null, NOW);
    when(politicianRepository.findById(accountId)).thenReturn(Optional.of(existing));
    when(partyRepository.findById(partyId))
        .thenReturn(Optional.of(Party.project(partyId, "Progressive Party", "PROG", 13, null, PartySpectrum.CENTER_LEFT, null, null, null, NOW)));

    service.onRepresentativeLinked(accountId, partyId, "Deputy", NOW);

    verify(politicianRepository).assignParty(accountId, partyId, "PROG", NOW);
    verify(politicianRepository).assignOffice(accountId, "Deputy", NOW);
    verify(politicianRepository, never()).save(any());
  }

  @Test
  void onPoliticianRegisteredOnlyAssignsPartyNeverOffice() {
    UUID accountId = UUID.randomUUID();
    UUID partyId = UUID.randomUUID();
    Politician existing = Politician.project(accountId, "Jane Doe", "janedoe", null, null, null, null, null, null, NOW);
    when(politicianRepository.findById(accountId)).thenReturn(Optional.of(existing));
    when(partyRepository.findById(partyId))
        .thenReturn(Optional.of(Party.project(partyId, "Progressive Party", "PROG", 13, null, PartySpectrum.CENTER_LEFT, null, null, null, NOW)));

    service.onPoliticianRegistered(accountId, partyId, NOW);

    verify(politicianRepository).assignParty(accountId, partyId, "PROG", NOW);
    verify(politicianRepository, never()).assignOffice(any(), any(), any());
  }

  @Test
  void onPartyRegisteredIsIdempotent() {
    UUID partyId = UUID.randomUUID();
    when(partyRepository.findById(partyId))
        .thenReturn(Optional.of(Party.project(partyId, "Progressive Party", "PROG", 13, null, null, null, null, null, NOW)));

    service.onPartyRegistered(partyId, "Progressive Party", "PROG", 13, "Jane Doe", null, NOW);

    verify(partyRepository, never()).save(any());
  }
}
