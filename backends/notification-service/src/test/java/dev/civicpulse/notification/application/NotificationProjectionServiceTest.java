package dev.civicpulse.notification.application;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import dev.civicpulse.notification.application.port.in.IngestNotificationUseCase;
import dev.civicpulse.notification.application.port.out.AffiliationLookupGateway;
import dev.civicpulse.notification.application.port.out.FundraiserLookupGateway;
import dev.civicpulse.notification.domain.model.NotificationCategory;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NotificationProjectionServiceTest {

  @Mock private IngestNotificationUseCase ingestNotificationUseCase;
  @Mock private AffiliationLookupGateway affiliationLookupGateway;
  @Mock private FundraiserLookupGateway fundraiserLookupGateway;

  private NotificationProjectionService service;

  @BeforeEach
  void setUp() {
    service = new NotificationProjectionService(ingestNotificationUseCase, affiliationLookupGateway, fundraiserLookupGateway);
  }

  @Test
  void onAffiliationConfirmedIngestsDirectlyUsingCitizenAccountId() {
    UUID affiliationId = UUID.randomUUID();
    UUID citizenId = UUID.randomUUID();

    service.onAffiliationConfirmed(affiliationId, citizenId, UUID.randomUUID());

    verify(ingestNotificationUseCase)
        .ingest(eq(citizenId), eq(NotificationCategory.PARTY), anyString(), anyString(), anyString(), anyString(), eq("affiliation-confirmed:" + affiliationId));
  }

  @Test
  void onMembershipFeeGeneratedSkipsWhenAffiliationNotFound() {
    UUID affiliationId = UUID.randomUUID();
    when(affiliationLookupGateway.lookupCitizenAccountId(affiliationId)).thenReturn(Optional.empty());

    service.onMembershipFeeGenerated(UUID.randomUUID(), affiliationId, 5000);

    verify(ingestNotificationUseCase, never()).ingest(any(), any(), any(), any(), any(), any(), any());
  }

  @Test
  void onMembershipFeeGeneratedIngestsWhenAffiliationResolved() {
    UUID affiliationId = UUID.randomUUID();
    UUID citizenId = UUID.randomUUID();
    UUID feeId = UUID.randomUUID();
    when(affiliationLookupGateway.lookupCitizenAccountId(affiliationId)).thenReturn(Optional.of(citizenId));

    service.onMembershipFeeGenerated(feeId, affiliationId, 5000);

    verify(ingestNotificationUseCase)
        .ingest(eq(citizenId), eq(NotificationCategory.PARTY), anyString(), anyString(), anyString(), anyString(), eq("membership-fee-generated:" + feeId));
  }

  @Test
  void onContributionReceivedIngestsDirectlyUsingSupporterAccountId() {
    UUID fundraiserId = UUID.randomUUID();
    UUID contributionId = UUID.randomUUID();
    UUID supporterId = UUID.randomUUID();

    service.onContributionReceived(fundraiserId, contributionId, supporterId, 3000);

    verify(ingestNotificationUseCase)
        .ingest(
            eq(supporterId), eq(NotificationCategory.CAMPAIGN), anyString(), anyString(), anyString(), anyString(),
            eq("contribution-received:" + contributionId));
  }

  @Test
  void onFundraiserGoalReachedSkipsWhenFundraiserNotFound() {
    UUID fundraiserId = UUID.randomUUID();
    when(fundraiserLookupGateway.lookupOrganizerAccountId(fundraiserId)).thenReturn(Optional.empty());

    service.onFundraiserGoalReached(fundraiserId, 100_000);

    verify(ingestNotificationUseCase, never()).ingest(any(), any(), any(), any(), any(), any(), any());
  }

  @Test
  void onFundraiserGoalReachedIngestsWhenOrganizerResolved() {
    UUID fundraiserId = UUID.randomUUID();
    UUID organizerId = UUID.randomUUID();
    when(fundraiserLookupGateway.lookupOrganizerAccountId(fundraiserId)).thenReturn(Optional.of(organizerId));

    service.onFundraiserGoalReached(fundraiserId, 100_000);

    verify(ingestNotificationUseCase)
        .ingest(
            eq(organizerId), eq(NotificationCategory.CAMPAIGN), anyString(), anyString(), anyString(), anyString(),
            eq("fundraiser-goal-reached:" + fundraiserId));
  }
}
