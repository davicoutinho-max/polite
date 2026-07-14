package dev.civicpulse.platformconfig.domain.event;

import java.time.Instant;
import java.util.UUID;

public record TranslationValueUpdated(UUID translationKeyId, String languageId, Instant occurredAt) implements DomainEvent {

  @Override
  public String topic() {
    return "translation-value-updated";
  }
}
