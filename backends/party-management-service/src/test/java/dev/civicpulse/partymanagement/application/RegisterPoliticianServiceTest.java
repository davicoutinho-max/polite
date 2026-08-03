package dev.civicpulse.partymanagement.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.civicpulse.partymanagement.application.port.in.RegisterPoliticianUseCase.RegisterPoliticianCommand;
import dev.civicpulse.partymanagement.application.port.out.EventPublisher;
import dev.civicpulse.partymanagement.application.port.out.IdentityProvisioningGateway;
import dev.civicpulse.partymanagement.application.port.out.IdentityProvisioningGateway.ProvisionedAccount;
import dev.civicpulse.partymanagement.application.port.out.PartyRepresentativeRepository;
import dev.civicpulse.partymanagement.application.port.out.RegistrationTokenGateway;
import dev.civicpulse.partymanagement.application.port.out.RegistrationTokenGateway.RedeemedToken;
import dev.civicpulse.partymanagement.domain.event.DomainEvent;
import dev.civicpulse.partymanagement.domain.event.PoliticianRegistered;
import dev.civicpulse.partymanagement.domain.event.RepresentativeLinked;
import dev.civicpulse.partymanagement.domain.event.RepresentativeRemoved;
import dev.civicpulse.partymanagement.domain.exception.InvalidRegistrationTokenException;
import dev.civicpulse.partymanagement.domain.model.PartyRepresentative;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RegisterPoliticianServiceTest {

  private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");
  private static final RegisterPoliticianCommand COMMAND =
      new RegisterPoliticianCommand("tok-123", "janedoe", "jane@example.com", "s3cret!", "cpf", "12345678901");

  @Mock private IdentityProvisioningGateway identityProvisioningGateway;
  @Mock private RegistrationTokenGateway registrationTokenGateway;
  @Mock private PartyRepresentativeRepository representativeRepository;
  @Mock private EventPublisher eventPublisher;

  private RegisterPoliticianService service;

  @BeforeEach
  void setUp() {
    service =
        new RegisterPoliticianService(
            identityProvisioningGateway, registrationTokenGateway, representativeRepository, eventPublisher, new ObjectMapper(), Clock.fixed(NOW, ZoneOffset.UTC));
  }

  private static String prefillFor(UUID partyId) {
    return "{\"name\":\"Jane Doe\",\"roleTitle\":\"Deputy\",\"state\":\"São Paulo\",\"partyId\":\"" + partyId + "\"}";
  }

  @Test
  void registersPoliticianAndPublishesBothEvents() {
    UUID partyId = UUID.randomUUID();
    UUID accountId = UUID.randomUUID();
    when(registrationTokenGateway.validate("tok-123")).thenReturn(new RedeemedToken(prefillFor(partyId)));
    when(identityProvisioningGateway.provisionPoliticianAccount("Jane Doe", "janedoe", "jane@example.com", "s3cret!", "cpf", "12345678901"))
        .thenReturn(new ProvisionedAccount(accountId, "Jane Doe", "janedoe"));
    when(representativeRepository.findByPoliticianAccountId(accountId)).thenReturn(Optional.empty());
    when(representativeRepository.save(any(PartyRepresentative.class))).thenAnswer(invocation -> invocation.getArgument(0));

    PartyRepresentative result = service.registerPolitician(partyId, COMMAND);

    assertThat(result.partyId()).isEqualTo(partyId);
    assertThat(result.politicianAccountId()).isEqualTo(accountId);
    assertThat(result.roleTitle()).contains("Deputy");

    ArgumentCaptor<DomainEvent> eventCaptor = ArgumentCaptor.forClass(DomainEvent.class);
    verify(eventPublisher, org.mockito.Mockito.times(2)).publish(eventCaptor.capture());
    assertThat(eventCaptor.getAllValues()).hasSize(2);
    assertThat(eventCaptor.getAllValues().get(0)).isInstanceOf(PoliticianRegistered.class);
    assertThat(eventCaptor.getAllValues().get(1)).isInstanceOf(RepresentativeLinked.class);
    assertThat(((RepresentativeLinked) eventCaptor.getAllValues().get(1)).state()).isEqualTo("São Paulo");
  }

  @Test
  void registeringAnAlreadyLinkedClaimedAccountForTheSamePartyIsANoOp() {
    // Identity turns a same-CPF registration into a claim of an already-synced account (see
    // Account.claim) — if that account was already linked to this same party by the earlier
    // government sync, re-inserting the link would violate party_representatives' unique index.
    UUID partyId = UUID.randomUUID();
    UUID accountId = UUID.randomUUID();
    PartyRepresentative existingLink = PartyRepresentative.link(UUID.randomUUID(), partyId, accountId, "Deputado Federal", NOW);
    when(registrationTokenGateway.validate("tok-123")).thenReturn(new RedeemedToken(prefillFor(partyId)));
    when(identityProvisioningGateway.provisionPoliticianAccount(any(), any(), any(), any(), any(), any()))
        .thenReturn(new ProvisionedAccount(accountId, "Jane Doe", "janedoe"));
    when(representativeRepository.findByPoliticianAccountId(accountId)).thenReturn(Optional.of(existingLink));

    PartyRepresentative result = service.registerPolitician(partyId, COMMAND);

    assertThat(result).isSameAs(existingLink);
    verify(representativeRepository, never()).save(any());
    verify(representativeRepository, never()).delete(any());
    verify(eventPublisher).publish(any(PoliticianRegistered.class));
    verify(eventPublisher, never()).publish(any(RepresentativeLinked.class));
  }

  @Test
  void registeringAClaimedAccountForADifferentPartyUnlinksTheOldOneFirst() {
    UUID oldPartyId = UUID.randomUUID();
    UUID newPartyId = UUID.randomUUID();
    UUID accountId = UUID.randomUUID();
    PartyRepresentative existingLink = PartyRepresentative.link(UUID.randomUUID(), oldPartyId, accountId, "Deputado Federal", NOW);
    when(registrationTokenGateway.validate("tok-123")).thenReturn(new RedeemedToken(prefillFor(newPartyId)));
    when(identityProvisioningGateway.provisionPoliticianAccount(any(), any(), any(), any(), any(), any()))
        .thenReturn(new ProvisionedAccount(accountId, "Jane Doe", "janedoe"));
    when(representativeRepository.findByPoliticianAccountId(accountId)).thenReturn(Optional.of(existingLink));
    when(representativeRepository.save(any(PartyRepresentative.class))).thenAnswer(invocation -> invocation.getArgument(0));

    PartyRepresentative result = service.registerPolitician(newPartyId, COMMAND);

    assertThat(result.partyId()).isEqualTo(newPartyId);
    verify(representativeRepository).delete(existingLink.id());

    ArgumentCaptor<DomainEvent> eventCaptor = ArgumentCaptor.forClass(DomainEvent.class);
    verify(eventPublisher, org.mockito.Mockito.times(3)).publish(eventCaptor.capture());
    assertThat(eventCaptor.getAllValues().get(0)).isInstanceOf(RepresentativeRemoved.class);
    assertThat(((RepresentativeRemoved) eventCaptor.getAllValues().get(0)).partyId()).isEqualTo(oldPartyId);
  }

  @Test
  void rejectsATokenIssuedForADifferentParty() {
    UUID partyId = UUID.randomUUID();
    UUID otherPartyId = UUID.randomUUID();
    when(registrationTokenGateway.validate("tok-123")).thenReturn(new RedeemedToken(prefillFor(otherPartyId)));

    assertThatThrownBy(() -> service.registerPolitician(partyId, COMMAND)).isInstanceOf(InvalidRegistrationTokenException.class);
  }
}
