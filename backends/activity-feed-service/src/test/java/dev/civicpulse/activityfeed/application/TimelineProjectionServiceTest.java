package dev.civicpulse.activityfeed.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import dev.civicpulse.activityfeed.application.port.out.FundraiserLookupGateway;
import dev.civicpulse.activityfeed.application.port.out.FundraiserLookupGateway.FundraiserSummary;
import dev.civicpulse.activityfeed.application.port.out.IdentityLookupGateway;
import dev.civicpulse.activityfeed.application.port.out.TimelineEventRepository;
import dev.civicpulse.activityfeed.domain.model.TimelineEvent;
import dev.civicpulse.activityfeed.domain.model.TimelineEventType;
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
class TimelineProjectionServiceTest {

  private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");

  @Mock private TimelineEventRepository timelineEventRepository;
  @Mock private IdentityLookupGateway identityLookupGateway;
  @Mock private FundraiserLookupGateway fundraiserLookupGateway;

  private TimelineProjectionService service;

  @BeforeEach
  void setUp() {
    service = new TimelineProjectionService(timelineEventRepository, identityLookupGateway, fundraiserLookupGateway, Clock.fixed(NOW, ZoneOffset.UTC));
  }

  @Test
  void ingestsVoteCastWhenNotAlreadySeen() {
    UUID politicianAccountId = UUID.randomUUID();
    UUID voteRecordId = UUID.randomUUID();
    when(timelineEventRepository.existsBySubjectAndSourceEventId(politicianAccountId, "vote-cast:" + voteRecordId)).thenReturn(false);
    when(identityLookupGateway.lookupDisplayName(politicianAccountId)).thenReturn(Optional.of("Jane Doe"));

    service.onVoteCast(voteRecordId, politicianAccountId, "PEC 33", "yes", NOW);

    ArgumentCaptor<TimelineEvent> captor = ArgumentCaptor.forClass(TimelineEvent.class);
    verify(timelineEventRepository).save(captor.capture());
    assertThat(captor.getValue().type()).isEqualTo(TimelineEventType.VOTE);
    assertThat(captor.getValue().actorNameDenormalized()).contains("Jane Doe");
  }

  @Test
  void skipsIngestionWhenSourceEventAlreadySeen() {
    UUID politicianAccountId = UUID.randomUUID();
    UUID voteRecordId = UUID.randomUUID();
    when(timelineEventRepository.existsBySubjectAndSourceEventId(politicianAccountId, "vote-cast:" + voteRecordId)).thenReturn(true);

    service.onVoteCast(voteRecordId, politicianAccountId, "PEC 33", "yes", NOW);

    verify(timelineEventRepository, never()).save(any());
  }

  @Test
  void fundraiserGoalReachedResolvesOrganizerViaLookup() {
    UUID fundraiserId = UUID.randomUUID();
    UUID organizerAccountId = UUID.randomUUID();
    when(fundraiserLookupGateway.lookupFundraiser(fundraiserId)).thenReturn(Optional.of(new FundraiserSummary(organizerAccountId, "Clean Water Now")));
    when(timelineEventRepository.existsBySubjectAndSourceEventId(organizerAccountId, "fundraiser-goal-reached:" + fundraiserId)).thenReturn(false);
    when(identityLookupGateway.lookupDisplayName(organizerAccountId)).thenReturn(Optional.empty());

    service.onFundraiserGoalReached(fundraiserId, 500_000L, NOW);

    ArgumentCaptor<TimelineEvent> captor = ArgumentCaptor.forClass(TimelineEvent.class);
    verify(timelineEventRepository).save(captor.capture());
    assertThat(captor.getValue().subjectAccountId()).isEqualTo(organizerAccountId);
    assertThat(captor.getValue().title()).contains("Clean Water Now");
  }

  @Test
  void fundraiserGoalReachedSkipsWhenFundraiserUnknown() {
    UUID fundraiserId = UUID.randomUUID();
    when(fundraiserLookupGateway.lookupFundraiser(fundraiserId)).thenReturn(Optional.empty());

    service.onFundraiserGoalReached(fundraiserId, 500_000L, NOW);

    verify(timelineEventRepository, never()).save(any());
  }
}
