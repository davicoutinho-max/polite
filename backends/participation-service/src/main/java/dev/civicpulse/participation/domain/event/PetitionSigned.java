package dev.civicpulse.participation.domain.event;

import java.time.Instant;
import java.util.UUID;

public record PetitionSigned(UUID petitionId, UUID citizenAccountId, Instant occurredAt) implements DomainEvent {

  @Override
  public String topic() {
    return "petition-signed";
  }
}
