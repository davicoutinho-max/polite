package dev.civicpulse.assistant.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public final class AssistantTopic {

  private final UUID id;
  private final String reference;
  private final String title;
  private final UUID legislativeItemId;
  private final Instant createdAt;

  private AssistantTopic(UUID id, String reference, String title, UUID legislativeItemId, Instant createdAt) {
    this.id = id;
    this.reference = requireNonBlank(reference, "reference");
    this.title = requireNonBlank(title, "title");
    this.legislativeItemId = legislativeItemId;
    this.createdAt = Objects.requireNonNull(createdAt);
  }

  public static AssistantTopic create(String reference, String title, UUID legislativeItemId, Instant now) {
    return new AssistantTopic(null, reference, title, legislativeItemId, now);
  }

  public static AssistantTopic reconstitute(UUID id, String reference, String title, UUID legislativeItemId, Instant createdAt) {
    return new AssistantTopic(id, reference, title, legislativeItemId, createdAt);
  }

  private static String requireNonBlank(String value, String field) {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException(field + " must not be blank");
    }
    return value;
  }

  public Optional<UUID> id() {
    return Optional.ofNullable(id);
  }

  public String reference() {
    return reference;
  }

  public String title() {
    return title;
  }

  public Optional<UUID> legislativeItemId() {
    return Optional.ofNullable(legislativeItemId);
  }

  public Instant createdAt() {
    return createdAt;
  }
}
