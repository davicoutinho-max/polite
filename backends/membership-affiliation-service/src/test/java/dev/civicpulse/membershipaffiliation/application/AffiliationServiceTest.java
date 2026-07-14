package dev.civicpulse.membershipaffiliation.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import dev.civicpulse.membershipaffiliation.application.port.out.AffiliationRepository;
import dev.civicpulse.membershipaffiliation.application.port.out.AffiliationStatusHistoryRepository;
import dev.civicpulse.membershipaffiliation.application.port.out.EventPublisher;
import dev.civicpulse.membershipaffiliation.application.port.out.MembershipCardRepository;
import dev.civicpulse.membershipaffiliation.domain.event.DomainEvent;
import dev.civicpulse.membershipaffiliation.domain.exception.ActiveAffiliationAlreadyExistsException;
import dev.civicpulse.membershipaffiliation.domain.model.Affiliation;
import dev.civicpulse.membershipaffiliation.domain.model.AffiliationStatus;
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
class AffiliationServiceTest {

  private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");

  @Mock private AffiliationRepository affiliationRepository;
  @Mock private AffiliationStatusHistoryRepository historyRepository;
  @Mock private MembershipCardRepository membershipCardRepository;
  @Mock private EventPublisher eventPublisher;

  private AffiliationService service;

  @BeforeEach
  void setUp() {
    service =
        new AffiliationService(affiliationRepository, historyRepository, membershipCardRepository, eventPublisher, Clock.fixed(NOW, ZoneOffset.UTC));
  }

  @Test
  void requestAffiliationEndsUpUnderReviewAndPublishesBothEvents() {
    UUID citizenId = UUID.randomUUID();
    UUID partyId = UUID.randomUUID();
    when(affiliationRepository.existsActiveByCitizenAndParty(citizenId, partyId)).thenReturn(false);
    when(affiliationRepository.save(any(Affiliation.class))).thenAnswer(invocation -> invocation.getArgument(0));

    Affiliation result = service.requestAffiliation(citizenId, partyId, "São Paulo");

    assertThat(result.status()).isEqualTo(AffiliationStatus.UNDER_REVIEW);
    verify(eventPublisher, times(2)).publish(org.mockito.ArgumentMatchers.any(DomainEvent.class));
    verify(historyRepository, times(2)).save(any());
  }

  @Test
  void rejectsRequestWhenActiveAffiliationAlreadyExists() {
    UUID citizenId = UUID.randomUUID();
    UUID partyId = UUID.randomUUID();
    when(affiliationRepository.existsActiveByCitizenAndParty(citizenId, partyId)).thenReturn(true);

    assertThatThrownBy(() -> service.requestAffiliation(citizenId, partyId, "São Paulo"))
        .isInstanceOf(ActiveAffiliationAlreadyExistsException.class);

    verify(affiliationRepository, never()).save(any());
  }

  @Test
  void confirmAffiliationIssuesACardAndPublishesConfirmedAndCardIssuedEvents() {
    UUID affiliationId = UUID.randomUUID();
    Affiliation affiliation = Affiliation.request(affiliationId, UUID.randomUUID(), UUID.randomUUID(), NOW);
    affiliation.startReview(NOW);
    affiliation.approveByParty(NOW);
    affiliation.sendToElectoralJustice(NOW);
    when(affiliationRepository.findById(affiliationId)).thenReturn(Optional.of(affiliation));
    when(affiliationRepository.save(any(Affiliation.class))).thenAnswer(invocation -> invocation.getArgument(0));
    when(membershipCardRepository.existsByMemberNumber(any())).thenReturn(false);

    Affiliation result = service.confirmAffiliation(affiliationId);

    assertThat(result.status()).isEqualTo(AffiliationStatus.AFFILIATED);
    verify(membershipCardRepository).save(any());
    verify(eventPublisher, times(2)).publish(any(DomainEvent.class));
  }
}
