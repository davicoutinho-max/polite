package dev.civicpulse.partymanagement.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import dev.civicpulse.partymanagement.application.port.in.RegisterPoliticianUseCase.RegisterPoliticianCommand;
import dev.civicpulse.partymanagement.application.port.out.EventPublisher;
import dev.civicpulse.partymanagement.application.port.out.IdentityProvisioningGateway;
import dev.civicpulse.partymanagement.application.port.out.IdentityProvisioningGateway.ProvisionedAccount;
import dev.civicpulse.partymanagement.application.port.out.PartyRepresentativeRepository;
import dev.civicpulse.partymanagement.domain.event.DomainEvent;
import dev.civicpulse.partymanagement.domain.event.PoliticianRegistered;
import dev.civicpulse.partymanagement.domain.event.RepresentativeLinked;
import dev.civicpulse.partymanagement.domain.model.PartyRepresentative;
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
class RegisterPoliticianServiceTest {

  private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");

  @Mock private IdentityProvisioningGateway identityProvisioningGateway;
  @Mock private PartyRepresentativeRepository representativeRepository;
  @Mock private EventPublisher eventPublisher;

  private RegisterPoliticianService service;

  @BeforeEach
  void setUp() {
    service = new RegisterPoliticianService(identityProvisioningGateway, representativeRepository, eventPublisher, Clock.fixed(NOW, ZoneOffset.UTC));
  }

  @Test
  void registersPoliticianAndPublishesBothEvents() {
    UUID partyId = UUID.randomUUID();
    UUID accountId = UUID.randomUUID();
    RegisterPoliticianCommand command =
        new RegisterPoliticianCommand("Jane Doe", "janedoe", "jane@example.com", "s3cret!", "cpf", "12345678901", "Deputy");

    when(identityProvisioningGateway.provisionPoliticianAccount("Jane Doe", "janedoe", "jane@example.com", "s3cret!", "cpf", "12345678901"))
        .thenReturn(new ProvisionedAccount(accountId, "Jane Doe", "janedoe"));
    when(representativeRepository.save(any(PartyRepresentative.class))).thenAnswer(invocation -> invocation.getArgument(0));

    PartyRepresentative result = service.registerPolitician(partyId, command);

    assertThat(result.partyId()).isEqualTo(partyId);
    assertThat(result.politicianAccountId()).isEqualTo(accountId);
    assertThat(result.roleTitle()).contains("Deputy");

    ArgumentCaptor<DomainEvent> eventCaptor = ArgumentCaptor.forClass(DomainEvent.class);
    verify(eventPublisher, org.mockito.Mockito.times(2)).publish(eventCaptor.capture());
    assertThat(eventCaptor.getAllValues()).hasSize(2);
    assertThat(eventCaptor.getAllValues().get(0)).isInstanceOf(PoliticianRegistered.class);
    assertThat(eventCaptor.getAllValues().get(1)).isInstanceOf(RepresentativeLinked.class);
  }
}
