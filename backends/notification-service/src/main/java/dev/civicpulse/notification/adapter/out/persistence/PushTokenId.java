package dev.civicpulse.notification.adapter.out.persistence;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

public class PushTokenId implements Serializable {

  private UUID accountId;
  private String platform;
  private String token;

  protected PushTokenId() {}

  public PushTokenId(UUID accountId, String platform, String token) {
    this.accountId = accountId;
    this.platform = platform;
    this.token = token;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof PushTokenId other)) return false;
    return Objects.equals(accountId, other.accountId) && Objects.equals(platform, other.platform) && Objects.equals(token, other.token);
  }

  @Override
  public int hashCode() {
    return Objects.hash(accountId, platform, token);
  }
}
