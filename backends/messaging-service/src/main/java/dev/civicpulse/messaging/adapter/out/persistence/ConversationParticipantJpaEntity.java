package dev.civicpulse.messaging.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "conversation_participants")
@IdClass(ConversationParticipantId.class)
public class ConversationParticipantJpaEntity {

  @Id
  @Column(name = "conversation_id")
  private UUID conversationId;

  @Id
  @Column(name = "account_id")
  private UUID accountId;

  @Column(name = "joined_at", nullable = false)
  private Instant joinedAt;

  @Column(name = "last_read_at")
  private Instant lastReadAt;

  protected ConversationParticipantJpaEntity() {}

  public ConversationParticipantJpaEntity(UUID conversationId, UUID accountId, Instant joinedAt, Instant lastReadAt) {
    this.conversationId = conversationId;
    this.accountId = accountId;
    this.joinedAt = joinedAt;
    this.lastReadAt = lastReadAt;
  }

  public UUID getConversationId() {
    return conversationId;
  }

  public UUID getAccountId() {
    return accountId;
  }

  public Instant getJoinedAt() {
    return joinedAt;
  }

  public Instant getLastReadAt() {
    return lastReadAt;
  }
}
