package dev.civicpulse.notification.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public final class PushToken {

  private final UUID accountId;
  private final NotificationPlatform platform;
  private final String token;
  private Instant updatedAt;

  private PushToken(UUID accountId, NotificationPlatform platform, String token, Instant updatedAt) {
    this.accountId = Objects.requireNonNull(accountId);
    this.platform = Objects.requireNonNull(platform);
    this.token = requireNonBlank(token);
    this.updatedAt = Objects.requireNonNull(updatedAt);
  }

  public static PushToken register(UUID accountId, NotificationPlatform platform, String token, Instant now) {
    return new PushToken(accountId, platform, token, now);
  }

  public static PushToken reconstitute(UUID accountId, NotificationPlatform platform, String token, Instant updatedAt) {
    return new PushToken(accountId, platform, token, updatedAt);
  }

  public void refresh(Instant now) {
    this.updatedAt = now;
  }

  private static String requireNonBlank(String value) {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException("token must not be blank");
    }
    return value;
  }

  public UUID accountId() {
    return accountId;
  }

  public NotificationPlatform platform() {
    return platform;
  }

  public String token() {
    return token;
  }

  public Instant updatedAt() {
    return updatedAt;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof PushToken other)) return false;
    return accountId.equals(other.accountId) && platform == other.platform && token.equals(other.token);
  }

  @Override
  public int hashCode() {
    return Objects.hash(accountId, platform, token);
  }
}
