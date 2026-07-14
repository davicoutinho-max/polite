package dev.civicpulse.activityfeed.application;

import dev.civicpulse.activityfeed.application.port.in.TimelineIngestionUseCase;
import dev.civicpulse.activityfeed.application.port.out.FundraiserLookupGateway;
import dev.civicpulse.activityfeed.application.port.out.IdentityLookupGateway;
import dev.civicpulse.activityfeed.application.port.out.TimelineEventRepository;
import dev.civicpulse.activityfeed.domain.model.TimelineEvent;
import dev.civicpulse.activityfeed.domain.model.TimelineEventType;
import java.time.Clock;
import java.time.Instant;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TimelineProjectionService implements TimelineIngestionUseCase {

  private static final Logger log = LoggerFactory.getLogger(TimelineProjectionService.class);

  private final TimelineEventRepository timelineEventRepository;
  private final IdentityLookupGateway identityLookupGateway;
  private final FundraiserLookupGateway fundraiserLookupGateway;
  private final Clock clock;

  public TimelineProjectionService(
      TimelineEventRepository timelineEventRepository,
      IdentityLookupGateway identityLookupGateway,
      FundraiserLookupGateway fundraiserLookupGateway,
      Clock clock) {
    this.timelineEventRepository = timelineEventRepository;
    this.identityLookupGateway = identityLookupGateway;
    this.fundraiserLookupGateway = fundraiserLookupGateway;
    this.clock = clock;
  }

  @Override
  @Transactional
  public void onPostPublished(UUID postId, UUID authorId, String kind, Instant occurredAt) {
    TimelineEventType type = "video".equals(kind) ? TimelineEventType.VIDEO : TimelineEventType.POST;
    String title = "video".equals(kind) ? "Published a video" : "Published a post";
    ingest(authorId, type, title, null, occurredAt, "post-published:" + postId, authorId);
  }

  @Override
  @Transactional
  public void onVoteCast(UUID voteRecordId, UUID politicianAccountId, String matter, String choice, Instant occurredAt) {
    String title = "Voted " + choice.toUpperCase() + " on " + matter;
    ingest(politicianAccountId, TimelineEventType.VOTE, title, null, occurredAt, "vote-cast:" + voteRecordId, politicianAccountId);
  }

  @Override
  @Transactional
  public void onLegislativeItemFiled(UUID legislativeItemId, UUID politicianAccountId, String category, String reference, Instant occurredAt) {
    TimelineEventType type = TimelineEventType.fromLegislativeItemCategory(category);
    String title = "Filed " + reference;
    ingest(
        politicianAccountId, type, title, null, occurredAt, "legislative-item-filed:" + legislativeItemId, politicianAccountId);
  }

  @Override
  @Transactional
  public void onLegislativeItemStatusChanged(UUID legislativeItemId, UUID politicianAccountId, String status, Instant occurredAt) {
    String title = "Legislative item status changed to " + status;
    ingest(
        politicianAccountId,
        TimelineEventType.STATUS_CHANGE,
        title,
        null,
        occurredAt,
        "legislative-item-status-changed:" + legislativeItemId + ":" + status,
        politicianAccountId);
  }

  @Override
  @Transactional
  public void onCommitteeMembershipChanged(UUID committeeMembershipId, UUID politicianAccountId, String name, Instant occurredAt) {
    String title = "Joined " + name;
    ingest(
        politicianAccountId,
        TimelineEventType.COMMITTEE,
        title,
        null,
        occurredAt,
        "committee-membership-changed:" + committeeMembershipId,
        politicianAccountId);
  }

  @Override
  @Transactional
  public void onPoliticianReassigned(UUID politicianAccountId, UUID partyId, Instant occurredAt) {
    String sourceEventId = "politician-reassigned:" + politicianAccountId + ":" + partyId + ":" + occurredAt.toEpochMilli();
    ingest(
        politicianAccountId, TimelineEventType.PARTY_CHANGE, "Changed party affiliation", null, occurredAt, sourceEventId, politicianAccountId);
  }

  @Override
  @Transactional
  public void onFundraiserGoalReached(UUID fundraiserId, long raisedCents, Instant occurredAt) {
    fundraiserLookupGateway
        .lookupFundraiser(fundraiserId)
        .ifPresentOrElse(
            fundraiser -> {
              String title = "Reached the fundraising goal for \"" + fundraiser.title() + "\"";
              ingest(
                  fundraiser.organizerAccountId(),
                  TimelineEventType.CAMPAIGN,
                  title,
                  null,
                  occurredAt,
                  "fundraiser-goal-reached:" + fundraiserId,
                  fundraiser.organizerAccountId());
            },
            () -> log.debug("Skipping FundraiserGoalReached for unknown fundraiser {}", fundraiserId));
  }

  private void ingest(
      UUID subjectAccountId, TimelineEventType type, String title, String detail, Instant occurredAt, String sourceEventId, UUID actorAccountId) {
    if (timelineEventRepository.existsBySubjectAndSourceEventId(subjectAccountId, sourceEventId)) {
      return;
    }
    String actorName = identityLookupGateway.lookupDisplayName(actorAccountId).orElse(null);
    timelineEventRepository.save(
        TimelineEvent.record(subjectAccountId, type, title, detail, occurredAt, sourceEventId, actorAccountId, actorName, clock.instant()));
  }
}
