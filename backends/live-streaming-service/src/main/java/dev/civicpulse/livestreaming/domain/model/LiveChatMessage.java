package dev.civicpulse.livestreaming.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/** Maps {@code live_chat_archive} — an OPTIONAL cold table, only populated when moderation or
 * compliance requires retaining chat beyond the Kafka topic's short retention window. Real-time
 * chat during the live window never touches this table or this service at all (see
 * LiveStreamingServiceApplication's scope note). */
public final class LiveChatMessage {

  private final Long id;
  private final UUID liveSessionId;
  private final UUID accountId;
  private final String body;
  private final Instant sentAt;

  private LiveChatMessage(Long id, UUID liveSessionId, UUID accountId, String body, Instant sentAt) {
    this.id = id;
    this.liveSessionId = Objects.requireNonNull(liveSessionId);
    this.accountId = Objects.requireNonNull(accountId);
    this.body = requireNonBlank(body);
    this.sentAt = Objects.requireNonNull(sentAt);
  }

  public static LiveChatMessage archive(UUID liveSessionId, UUID accountId, String body, Instant sentAt) {
    return new LiveChatMessage(null, liveSessionId, accountId, body, sentAt);
  }

  public static LiveChatMessage reconstitute(Long id, UUID liveSessionId, UUID accountId, String body, Instant sentAt) {
    return new LiveChatMessage(id, liveSessionId, accountId, body, sentAt);
  }

  private static String requireNonBlank(String value) {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException("body must not be blank");
    }
    return value;
  }

  public Optional<Long> id() {
    return Optional.ofNullable(id);
  }

  public UUID liveSessionId() {
    return liveSessionId;
  }

  public UUID accountId() {
    return accountId;
  }

  public String body() {
    return body;
  }

  public Instant sentAt() {
    return sentAt;
  }
}
