package dev.civicpulse.platformconfig.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import dev.civicpulse.platformconfig.application.port.out.EventPublisher;
import dev.civicpulse.platformconfig.application.port.out.IdentityProvisioningGateway;
import dev.civicpulse.platformconfig.application.port.out.IdentityProvisioningGateway.ProvisionedAccount;
import dev.civicpulse.platformconfig.application.port.out.PartyRegistryRepository;
import dev.civicpulse.platformconfig.domain.event.PartyRegistered;
import dev.civicpulse.platformconfig.domain.exception.DuplicatePartyRegistrationException;
import dev.civicpulse.platformconfig.domain.model.PartyRegistryEntry;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RegisterPartyServiceTest {

  private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");

  @Mock private PartyRegistryRepository partyRegistryRepository;
  @Mock private IdentityProvisioningGateway identityProvisioningGateway;
  @Mock private EventPublisher eventPublisher;

  private RegisterPartyService service;

  @BeforeEach
  void setUp() {
    service = new RegisterPartyService(partyRegistryRepository, identityProvisioningGateway, eventPublisher, Clock.fixed(NOW, ZoneOffset.UTC));
  }

  @Test
  void registersPartyAndPublishesEvent() {
    UUID accountId = UUID.randomUUID();
    when(partyRegistryRepository.existsByAcronym("PROG")).thenReturn(false);
    when(partyRegistryRepository.existsByNumber(13)).thenReturn(false);
    when(identityProvisioningGateway.provisionPartyAccount("Progressive Party", "progressive", "party@example.com", "s3cret!", "cnpj", "11222333000181"))
        .thenReturn(new ProvisionedAccount(accountId, "Progressive Party", "progressive"));
    when(partyRegistryRepository.save(any(PartyRegistryEntry.class))).thenAnswer(invocation -> invocation.getArgument(0));

    PartyRegistryEntry result =
        service.registerParty(
            "Progressive Party", "PROG", 13, "Jane Doe", "Progressivism", "progressive", "party@example.com", "s3cret!", "cnpj", "11222333000181");

    assertThat(result.acronym()).isEqualTo("PROG");
    assertThat(result.id()).isEqualTo(accountId);
    ArgumentCaptor<PartyRegistered> captor = ArgumentCaptor.forClass(PartyRegistered.class);
    verify(eventPublisher).publish(captor.capture());
    assertThat(captor.getValue().acronym()).isEqualTo("PROG");
    assertThat(captor.getValue().number()).isEqualTo(13);
    assertThat(captor.getValue().partyId()).isEqualTo(accountId);
  }

  @Test
  void rejectsDuplicateAcronym() {
    when(partyRegistryRepository.existsByAcronym("PROG")).thenReturn(true);

    assertThatThrownBy(
            () -> service.registerParty("Progressive Party", "PROG", 13, null, null, "progressive", "party@example.com", "s3cret!", "cnpj", "11222333000181"))
        .isInstanceOf(DuplicatePartyRegistrationException.class);
  }

  @Test
  void rejectsDuplicateNumber() {
    when(partyRegistryRepository.existsByAcronym("PROG")).thenReturn(false);
    when(partyRegistryRepository.existsByNumber(13)).thenReturn(true);

    assertThatThrownBy(
            () -> service.registerParty("Progressive Party", "PROG", 13, null, null, "progressive", "party@example.com", "s3cret!", "cnpj", "11222333000181"))
        .isInstanceOf(DuplicatePartyRegistrationException.class);
  }
}
