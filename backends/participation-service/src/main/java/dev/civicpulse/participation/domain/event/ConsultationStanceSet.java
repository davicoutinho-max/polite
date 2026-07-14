package dev.civicpulse.participation.domain.event;

import java.time.Instant;
import java.util.UUID;

public record ConsultationStanceSet(UUID consultationId, UUID citizenAccountId, String stance, Instant occurredAt) implements DomainEvent {

  @Override
  public String topic() {
    return "consultation-stance-set";
  }
}
