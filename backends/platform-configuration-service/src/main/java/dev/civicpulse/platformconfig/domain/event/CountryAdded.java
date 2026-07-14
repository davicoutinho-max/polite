package dev.civicpulse.platformconfig.domain.event;

import java.time.Instant;
import java.util.UUID;

public record CountryAdded(UUID countryId, Instant occurredAt) implements DomainEvent {

  @Override
  public String topic() {
    return "country-added";
  }
}
