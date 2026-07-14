package dev.civicpulse.analytics.adapter.in.messaging;

import dev.civicpulse.analytics.adapter.in.messaging.dto.CommentAddedMessage;
import dev.civicpulse.analytics.adapter.in.messaging.dto.FollowCreatedMessage;
import dev.civicpulse.analytics.adapter.in.messaging.dto.FollowRemovedMessage;
import dev.civicpulse.analytics.adapter.in.messaging.dto.PostLikedMessage;
import dev.civicpulse.analytics.adapter.in.messaging.dto.PostPublishedMessage;
import dev.civicpulse.analytics.application.port.in.EngagementIngestionUseCase;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/** Translates the events listed in schema.sql's "Domain events consumed" section into
 * {@link EngagementIngestionUseCase} calls. This is the only place in the service that knows
 * about Kafka topic names/payload shapes. */
@Component
class AnalyticsProjectionListener {

  private final EngagementIngestionUseCase engagementIngestionUseCase;

  AnalyticsProjectionListener(EngagementIngestionUseCase engagementIngestionUseCase) {
    this.engagementIngestionUseCase = engagementIngestionUseCase;
  }

  @KafkaListener(topics = "post-published", groupId = "analytics-service")
  void onPostPublished(PostPublishedMessage message) {
    engagementIngestionUseCase.onPostPublished(message.postId(), message.authorId(), message.kind(), message.occurredAt());
  }

  @KafkaListener(topics = "post-liked", groupId = "analytics-service")
  void onPostLiked(PostLikedMessage message) {
    engagementIngestionUseCase.onPostLiked(message.postId(), message.accountId(), message.occurredAt());
  }

  @KafkaListener(topics = "comment-added", groupId = "analytics-service")
  void onCommentAdded(CommentAddedMessage message) {
    engagementIngestionUseCase.onCommentAdded(message.postId(), message.commentId(), message.occurredAt());
  }

  @KafkaListener(topics = "follow-created", groupId = "analytics-service")
  void onFollowCreated(FollowCreatedMessage message) {
    engagementIngestionUseCase.onFollowCreated(message.followerAccountId(), message.targetType(), message.targetId(), message.occurredAt());
  }

  @KafkaListener(topics = "follow-removed", groupId = "analytics-service")
  void onFollowRemoved(FollowRemovedMessage message) {
    engagementIngestionUseCase.onFollowRemoved(message.followerAccountId(), message.targetType(), message.targetId(), message.occurredAt());
  }
}
