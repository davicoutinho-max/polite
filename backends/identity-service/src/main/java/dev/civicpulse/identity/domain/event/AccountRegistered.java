package dev.civicpulse.identity.domain.event;

import java.time.Instant;
import java.util.UUID;

/** Consumed by Directory Service (to project a politician/party row) and by Party Management. */
public record AccountRegistered(UUID accountId, String accountType, String documentHash, Instant occurredAt)
    implements DomainEvent {

  @Override
  public String topic() {
    return "account-registered";
  }
}
