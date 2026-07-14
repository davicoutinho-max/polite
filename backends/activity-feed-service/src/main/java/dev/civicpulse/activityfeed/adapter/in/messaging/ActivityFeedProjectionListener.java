package dev.civicpulse.activityfeed.adapter.in.messaging;

import dev.civicpulse.activityfeed.adapter.in.messaging.dto.CommitteeMembershipChangedMessage;
import dev.civicpulse.activityfeed.adapter.in.messaging.dto.FundraiserGoalReachedMessage;
import dev.civicpulse.activityfeed.adapter.in.messaging.dto.LegislativeItemFiledMessage;
import dev.civicpulse.activityfeed.adapter.in.messaging.dto.LegislativeItemStatusChangedMessage;
import dev.civicpulse.activityfeed.adapter.in.messaging.dto.PoliticianReassignedMessage;
import dev.civicpulse.activityfeed.adapter.in.messaging.dto.PostPublishedMessage;
import dev.civicpulse.activityfeed.adapter.in.messaging.dto.VoteCastMessage;
import dev.civicpulse.activityfeed.application.port.in.TimelineIngestionUseCase;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/** Translates the events listed in schema.sql's "Domain events consumed" section into
 * {@link TimelineIngestionUseCase} calls. This is the only place in the service that knows
 * about Kafka topic names/payload shapes. */
@Component
class ActivityFeedProjectionListener {

  private final TimelineIngestionUseCase timelineIngestionUseCase;

  ActivityFeedProjectionListener(TimelineIngestionUseCase timelineIngestionUseCase) {
    this.timelineIngestionUseCase = timelineIngestionUseCase;
  }

  @KafkaListener(topics = "post-published", groupId = "activity-feed-service")
  void onPostPublished(PostPublishedMessage message) {
    timelineIngestionUseCase.onPostPublished(message.postId(), message.authorId(), message.kind(), message.occurredAt());
  }

  @KafkaListener(topics = "vote-cast", groupId = "activity-feed-service")
  void onVoteCast(VoteCastMessage message) {
    timelineIngestionUseCase.onVoteCast(message.voteRecordId(), message.politicianAccountId(), message.matter(), message.choice(), message.occurredAt());
  }

  @KafkaListener(topics = "legislative-item-filed", groupId = "activity-feed-service")
  void onLegislativeItemFiled(LegislativeItemFiledMessage message) {
    timelineIngestionUseCase.onLegislativeItemFiled(
        message.legislativeItemId(), message.politicianAccountId(), message.category(), message.reference(), message.occurredAt());
  }

  @KafkaListener(topics = "legislative-item-status-changed", groupId = "activity-feed-service")
  void onLegislativeItemStatusChanged(LegislativeItemStatusChangedMessage message) {
    timelineIngestionUseCase.onLegislativeItemStatusChanged(
        message.legislativeItemId(), message.politicianAccountId(), message.status(), message.occurredAt());
  }

  @KafkaListener(topics = "committee-membership-changed", groupId = "activity-feed-service")
  void onCommitteeMembershipChanged(CommitteeMembershipChangedMessage message) {
    timelineIngestionUseCase.onCommitteeMembershipChanged(
        message.committeeMembershipId(), message.politicianAccountId(), message.name(), message.occurredAt());
  }

  @KafkaListener(topics = "politician-reassigned", groupId = "activity-feed-service")
  void onPoliticianReassigned(PoliticianReassignedMessage message) {
    timelineIngestionUseCase.onPoliticianReassigned(message.politicianAccountId(), message.partyId(), message.occurredAt());
  }

  @KafkaListener(topics = "fundraiser-goal-reached", groupId = "activity-feed-service")
  void onFundraiserGoalReached(FundraiserGoalReachedMessage message) {
    timelineIngestionUseCase.onFundraiserGoalReached(message.fundraiserId(), message.raisedCents(), message.occurredAt());
  }
}
