package dev.civicpulse.fundraising.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import dev.civicpulse.fundraising.application.port.out.ContributionRepository;
import dev.civicpulse.fundraising.application.port.out.EventPublisher;
import dev.civicpulse.fundraising.application.port.out.FundraiserRepository;
import dev.civicpulse.fundraising.application.port.out.PaymentIntentLookupGateway;
import dev.civicpulse.fundraising.application.port.out.PaymentIntentLookupGateway.PaymentIntentSummary;
import dev.civicpulse.fundraising.domain.event.ContributionReceived;
import dev.civicpulse.fundraising.domain.event.FundraiserGoalReached;
import dev.civicpulse.fundraising.domain.exception.FundraiserNotFoundException;
import dev.civicpulse.fundraising.domain.exception.LedgerNotPublicException;
import dev.civicpulse.fundraising.domain.model.Fundraiser;
import dev.civicpulse.fundraising.domain.model.FundraiserCategory;
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
class ContributionServiceTest {

  private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");

  @Mock private FundraiserRepository fundraiserRepository;
  @Mock private ContributionRepository contributionRepository;
  @Mock private PaymentIntentLookupGateway paymentIntentLookupGateway;
  @Mock private EventPublisher eventPublisher;

  private ContributionService service;

  @BeforeEach
  void setUp() {
    service =
        new ContributionService(
            fundraiserRepository, contributionRepository, paymentIntentLookupGateway, eventPublisher, Clock.fixed(NOW, ZoneOffset.UTC));
  }

  @Test
  void onPaymentCapturedSkipsWhenAlreadyProcessed() {
    UUID paymentIntentId = UUID.randomUUID();
    when(contributionRepository.existsByPaymentIntentId(paymentIntentId)).thenReturn(true);

    service.onPaymentCaptured(UUID.randomUUID(), paymentIntentId, 5000);

    verify(fundraiserRepository, never()).findById(any());
  }

  @Test
  void onPaymentCapturedThrowsWhenFundraiserMissing() {
    UUID fundraiserId = UUID.randomUUID();
    UUID paymentIntentId = UUID.randomUUID();
    when(contributionRepository.existsByPaymentIntentId(paymentIntentId)).thenReturn(false);
    when(fundraiserRepository.findById(fundraiserId)).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.onPaymentCaptured(fundraiserId, paymentIntentId, 5000)).isInstanceOf(FundraiserNotFoundException.class);
  }

  @Test
  void onPaymentCapturedRecordsContributionAndPublishesEvent() {
    UUID fundraiserId = UUID.randomUUID();
    UUID paymentIntentId = UUID.randomUUID();
    UUID supporterId = UUID.randomUUID();
    Fundraiser fundraiser = Fundraiser.create(fundraiserId, UUID.randomUUID(), "title", null, FundraiserCategory.SOCIAL, 100_000, null, true, NOW);
    when(contributionRepository.existsByPaymentIntentId(paymentIntentId)).thenReturn(false);
    when(fundraiserRepository.findById(fundraiserId)).thenReturn(Optional.of(fundraiser));
    when(paymentIntentLookupGateway.lookup(paymentIntentId)).thenReturn(new PaymentIntentSummary(supporterId, 30_000));
    when(contributionRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

    service.onPaymentCaptured(fundraiserId, paymentIntentId, 30_000);

    verify(fundraiserRepository).save(fundraiser);
    assertThat(fundraiser.raisedCents()).isEqualTo(30_000);
    verify(eventPublisher).publish(any(ContributionReceived.class));
    verify(eventPublisher, never()).publish(any(FundraiserGoalReached.class));
  }

  @Test
  void onPaymentCapturedPublishesGoalReachedWhenCrossingGoal() {
    UUID fundraiserId = UUID.randomUUID();
    UUID paymentIntentId = UUID.randomUUID();
    UUID supporterId = UUID.randomUUID();
    Fundraiser fundraiser = Fundraiser.create(fundraiserId, UUID.randomUUID(), "title", null, FundraiserCategory.SOCIAL, 100_000, null, true, NOW);
    when(contributionRepository.existsByPaymentIntentId(paymentIntentId)).thenReturn(false);
    when(fundraiserRepository.findById(fundraiserId)).thenReturn(Optional.of(fundraiser));
    when(paymentIntentLookupGateway.lookup(paymentIntentId)).thenReturn(new PaymentIntentSummary(supporterId, 100_000));
    when(contributionRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

    service.onPaymentCaptured(fundraiserId, paymentIntentId, 100_000);

    verify(eventPublisher).publish(any(FundraiserGoalReached.class));
  }

  @Test
  void listByFundraiserThrowsWhenLedgerNotPublic() {
    UUID fundraiserId = UUID.randomUUID();
    Fundraiser fundraiser = Fundraiser.create(fundraiserId, UUID.randomUUID(), "title", null, FundraiserCategory.SOCIAL, 100_000, null, false, NOW);
    when(fundraiserRepository.findById(fundraiserId)).thenReturn(Optional.of(fundraiser));

    assertThatThrownBy(() -> service.listByFundraiser(fundraiserId)).isInstanceOf(LedgerNotPublicException.class);
  }

  @Test
  void listByFundraiserReturnsContributionsWhenLedgerPublic() {
    UUID fundraiserId = UUID.randomUUID();
    Fundraiser fundraiser = Fundraiser.create(fundraiserId, UUID.randomUUID(), "title", null, FundraiserCategory.SOCIAL, 100_000, null, true, NOW);
    when(fundraiserRepository.findById(fundraiserId)).thenReturn(Optional.of(fundraiser));

    service.listByFundraiser(fundraiserId);

    verify(contributionRepository).findByFundraiserId(fundraiserId);
  }
}
