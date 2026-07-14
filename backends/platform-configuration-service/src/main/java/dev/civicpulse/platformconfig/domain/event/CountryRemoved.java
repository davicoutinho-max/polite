package dev.civicpulse.platformconfig.domain.event;

import java.time.Instant;
import java.util.UUID;

public record CountryRemoved(UUID countryId, Instant occurredAt) implements DomainEvent {

  @Override
  public String topic() {
    return "country-removed";
  }
}
