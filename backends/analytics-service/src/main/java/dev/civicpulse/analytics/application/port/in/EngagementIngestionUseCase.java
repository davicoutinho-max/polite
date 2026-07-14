package dev.civicpulse.analytics.application.port.in;

import java.time.Instant;
import java.util.UUID;

/** Translates each consumed domain event into an {@code EngagementEvent} row — see schema.sql's
 * "Domain events consumed" list for the full author/actor/content-type mapping rationale. */
public interface EngagementIngestionUseCase {

  void onPostPublished(UUID postId, UUID authorId, String kind, Instant occurredAt);

  void onPostLiked(UUID postId, UUID accountId, Instant occurredAt);

  void onCommentAdded(UUID postId, UUID commentId, Instant occurredAt);

  void onFollowCreated(UUID followerAccountId, String targetType, UUID targetId, Instant occurredAt);

  void onFollowRemoved(UUID followerAccountId, String targetType, UUID targetId, Instant occurredAt);
}
