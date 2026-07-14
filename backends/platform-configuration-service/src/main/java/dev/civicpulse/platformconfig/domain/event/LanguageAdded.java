package dev.civicpulse.platformconfig.domain.event;

import java.time.Instant;

public record LanguageAdded(String languageId, Instant occurredAt) implements DomainEvent {

  @Override
  public String topic() {
    return "language-added";
  }
}
