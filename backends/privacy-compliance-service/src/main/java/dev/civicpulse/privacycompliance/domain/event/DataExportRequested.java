package dev.civicpulse.privacycompliance.domain.event;

import java.time.Instant;
import java.util.UUID;

public record DataExportRequested(UUID requestId, UUID accountId, Instant occurredAt) implements DomainEvent {

  @Override
  public String topic() {
    return "data-export-requested";
  }
}
