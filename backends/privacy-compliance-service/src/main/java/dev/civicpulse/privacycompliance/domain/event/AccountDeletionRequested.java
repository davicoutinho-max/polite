package dev.civicpulse.privacycompliance.domain.event;

import java.time.Instant;
import java.util.UUID;

public record AccountDeletionRequested(UUID requestId, UUID accountId, Instant occurredAt) implements DomainEvent {

  @Override
  public String topic() {
    return "account-deletion-requested";
  }
}
