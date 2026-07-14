package dev.civicpulse.messaging.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public final class ConversationParticipant {

  private final UUID conversationId;
  private final UUID accountId;
  private final Instant joinedAt;
  private Instant lastReadAt;

  private ConversationParticipant(UUID conversationId, UUID accountId, Instant joinedAt, Instant lastReadAt) {
    this.conversationId = Objects.requireNonNull(conversationId);
    this.accountId = Objects.requireNonNull(accountId);
    this.joinedAt = Objects.requireNonNull(joinedAt);
    this.lastReadAt = lastReadAt;
  }

  public static ConversationParticipant join(UUID conversationId, UUID accountId, Instant now) {
    return new ConversationParticipant(conversationId, accountId, now, null);
  }

  public static ConversationParticipant reconstitute(UUID conversationId, UUID accountId, Instant joinedAt, Instant lastReadAt) {
    return new ConversationParticipant(conversationId, accountId, joinedAt, lastReadAt);
  }

  public void markRead(Instant now) {
    this.lastReadAt = now;
  }

  public UUID conversationId() {
    return conversationId;
  }

  public UUID accountId() {
    return accountId;
  }

  public Instant joinedAt() {
    return joinedAt;
  }

  public Optional<Instant> lastReadAt() {
    return Optional.ofNullable(lastReadAt);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof ConversationParticipant other)) return false;
    return conversationId.equals(other.conversationId) && accountId.equals(other.accountId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(conversationId, accountId);
  }
}
