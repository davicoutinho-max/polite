package dev.civicpulse.notification.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/** No framework imports — the domain core of the hexagonal architecture (see
 * docs/architecture/system-architecture.html). {@code sourceEventId} is the upstream event's own
 * id, used to dedupe redelivered Kafka messages at the application layer (see schema.sql's
 * comment — the partitioned unique index alone can't span partitions perfectly). */
public final class Notification {

  private final UUID id;
  private final UUID recipientAccountId;
  private final NotificationCategory category;
  private final String icon;
  private final String title;
  private final String message;
  private final String link;
  private final String sourceEventId;
  private boolean read;
  private final Instant createdAt;

  private Notification(
      UUID id,
      UUID recipientAccountId,
      NotificationCategory category,
      String icon,
      String title,
      String message,
      String link,
      String sourceEventId,
      boolean read,
      Instant createdAt) {
    this.id = Objects.requireNonNull(id);
    this.recipientAccountId = Objects.requireNonNull(recipientAccountId);
    this.category = Objects.requireNonNull(category);
    this.icon = icon;
    this.title = requireNonBlank(title, "title");
    this.message = requireNonBlank(message, "message");
    this.link = link;
    this.sourceEventId = requireNonBlank(sourceEventId, "sourceEventId");
    this.read = read;
    this.createdAt = Objects.requireNonNull(createdAt);
  }

  public static Notification create(
      UUID id,
      UUID recipientAccountId,
      NotificationCategory category,
      String icon,
      String title,
      String message,
      String link,
      String sourceEventId,
      Instant now) {
    return new Notification(id, recipientAccountId, category, icon, title, message, link, sourceEventId, false, now);
  }

  public static Notification reconstitute(
      UUID id,
      UUID recipientAccountId,
      NotificationCategory category,
      String icon,
      String title,
      String message,
      String link,
      String sourceEventId,
      boolean read,
      Instant createdAt) {
    return new Notification(id, recipientAccountId, category, icon, title, message, link, sourceEventId, read, createdAt);
  }

  public void markRead() {
    this.read = true;
  }

  private static String requireNonBlank(String value, String field) {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException(field + " must not be blank");
    }
    return value;
  }

  public UUID id() {
    return id;
  }

  public UUID recipientAccountId() {
    return recipientAccountId;
  }

  public NotificationCategory category() {
    return category;
  }

  public Optional<String> icon() {
    return Optional.ofNullable(icon);
  }

  public String title() {
    return title;
  }

  public String message() {
    return message;
  }

  public Optional<String> link() {
    return Optional.ofNullable(link);
  }

  public String sourceEventId() {
    return sourceEventId;
  }

  public boolean read() {
    return read;
  }

  public Instant createdAt() {
    return createdAt;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Notification other)) return false;
    return id.equals(other.id);
  }

  @Override
  public int hashCode() {
    return id.hashCode();
  }
}
