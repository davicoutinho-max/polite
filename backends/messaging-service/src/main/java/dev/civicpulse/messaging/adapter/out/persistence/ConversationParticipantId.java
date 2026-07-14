package dev.civicpulse.messaging.adapter.out.persistence;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

public class ConversationParticipantId implements Serializable {

  private UUID conversationId;
  private UUID accountId;

  protected ConversationParticipantId() {}

  public ConversationParticipantId(UUID conversationId, UUID accountId) {
    this.conversationId = conversationId;
    this.accountId = accountId;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof ConversationParticipantId other)) return false;
    return Objects.equals(conversationId, other.conversationId) && Objects.equals(accountId, other.accountId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(conversationId, accountId);
  }
}
