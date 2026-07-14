package dev.civicpulse.platformconfig.domain.event;

import java.time.Instant;

public record DefaultLanguageChanged(String languageId, Instant occurredAt) implements DomainEvent {

  @Override
  public String topic() {
    return "default-language-changed";
  }
}
