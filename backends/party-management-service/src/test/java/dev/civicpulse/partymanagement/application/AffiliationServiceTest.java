package dev.civicpulse.partymanagement.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import dev.civicpulse.partymanagement.application.port.out.AffiliationRequestRepository;
import dev.civicpulse.partymanagement.application.port.out.EventPublisher;
import dev.civicpulse.partymanagement.application.port.out.PartyMemberRepository;
import dev.civicpulse.partymanagement.domain.event.AffiliationRequestApproved;
import dev.civicpulse.partymanagement.domain.model.AffiliationRequest;
import dev.civicpulse.partymanagement.domain.model.PartyMember;
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
class AffiliationServiceTest {

  private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");

  @Mock private AffiliationRequestRepository requestRepository;
  @Mock private PartyMemberRepository memberRepository;
  @Mock private EventPublisher eventPublisher;

  private AffiliationService service;

  @BeforeEach
  void setUp() {
    service = new AffiliationService(requestRepository, memberRepository, eventPublisher, Clock.fixed(NOW, ZoneOffset.UTC));
  }

  @Test
  void onAffiliationRequestedCreatesRowOnce() {
    UUID requestId = UUID.randomUUID();
    when(requestRepository.findById(requestId)).thenReturn(Optional.empty());

    service.onAffiliationRequested(requestId, UUID.randomUUID(), UUID.randomUUID(), "São Paulo", NOW);

    verify(requestRepository).save(any(AffiliationRequest.class));
  }

  @Test
  void onAffiliationRequestedIsIdempotent() {
    UUID requestId = UUID.randomUUID();
    when(requestRepository.findById(requestId))
        .thenReturn(Optional.of(AffiliationRequest.create(requestId, UUID.randomUUID(), UUID.randomUUID(), null, NOW)));

    service.onAffiliationRequested(requestId, UUID.randomUUID(), UUID.randomUUID(), null, NOW);

    verify(requestRepository, never()).save(any());
  }

  @Test
  void approvingAdmitsPartyMemberAndPublishesEvent() {
    UUID requestId = UUID.randomUUID();
    UUID partyId = UUID.randomUUID();
    UUID citizenId = UUID.randomUUID();
    AffiliationRequest request = AffiliationRequest.create(requestId, partyId, citizenId, "São Paulo", NOW);
    when(requestRepository.findById(requestId)).thenReturn(Optional.of(request));
    when(requestRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

    service.approve(requestId);

    ArgumentCaptor<PartyMember> memberCaptor = ArgumentCaptor.forClass(PartyMember.class);
    verify(memberRepository).save(memberCaptor.capture());
    assertThat(memberCaptor.getValue().partyId()).isEqualTo(partyId);
    assertThat(memberCaptor.getValue().citizenAccountId()).isEqualTo(citizenId);

    ArgumentCaptor<AffiliationRequestApproved> eventCaptor = ArgumentCaptor.forClass(AffiliationRequestApproved.class);
    verify(eventPublisher).publish(eventCaptor.capture());
    assertThat(eventCaptor.getValue().requestId()).isEqualTo(requestId);
  }
}
