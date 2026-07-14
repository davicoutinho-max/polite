package dev.civicpulse.activityfeed.application.port.in;

import java.time.Instant;
import java.util.UUID;

/** Translates each consumed domain event into a {@code TimelineEvent} row — see
 * schema.sql's "Domain events consumed" list for the full subject/actor/type mapping rationale. */
public interface TimelineIngestionUseCase {

  void onPostPublished(UUID postId, UUID authorId, String kind, Instant occurredAt);

  void onVoteCast(UUID voteRecordId, UUID politicianAccountId, String matter, String choice, Instant occurredAt);

  void onLegislativeItemFiled(UUID legislativeItemId, UUID politicianAccountId, String category, String reference, Instant occurredAt);

  void onLegislativeItemStatusChanged(UUID legislativeItemId, UUID politicianAccountId, String status, Instant occurredAt);

  void onCommitteeMembershipChanged(UUID committeeMembershipId, UUID politicianAccountId, String name, Instant occurredAt);

  void onPoliticianReassigned(UUID politicianAccountId, UUID partyId, Instant occurredAt);

  void onFundraiserGoalReached(UUID fundraiserId, long raisedCents, Instant occurredAt);
}
