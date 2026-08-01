package dev.civicpulse.feedcontent.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/** A politician/party's own real Facebook Page, Instagram Business account, or X account,
 * connected via that platform's real OAuth flow (see the *OAuthGateway ports) — {@code
 * accessToken} is the real token used to publish on their behalf later. One row per account per
 * platform; reconnecting replaces the previous token rather than accumulating rows, since only
 * the latest token is ever usable. */
public final class SocialConnection {

  private final UUID id;
  private final UUID accountId;
  private final SocialPlatform platform;
  private String accessToken;
  private String externalAccountId;
  private String externalAccountName;
  private Instant connectedAt;

  private SocialConnection(
      UUID id,
      UUID accountId,
      SocialPlatform platform,
      String accessToken,
      String externalAccountId,
      String externalAccountName,
      Instant connectedAt) {
    this.id = Objects.requireNonNull(id);
    this.accountId = Objects.requireNonNull(accountId);
    this.platform = Objects.requireNonNull(platform);
    this.accessToken = requireNonBlank(accessToken, "accessToken");
    this.externalAccountId = requireNonBlank(externalAccountId, "externalAccountId");
    this.externalAccountName = externalAccountName;
    this.connectedAt = Objects.requireNonNull(connectedAt);
  }

  public static SocialConnection connect(
      UUID accountId, SocialPlatform platform, String accessToken, String externalAccountId, String externalAccountName, Instant now) {
    return new SocialConnection(UUID.randomUUID(), accountId, platform, accessToken, externalAccountId, externalAccountName, now);
  }

  public static SocialConnection reconstitute(
      UUID id,
      UUID accountId,
      SocialPlatform platform,
      String accessToken,
      String externalAccountId,
      String externalAccountName,
      Instant connectedAt) {
    return new SocialConnection(id, accountId, platform, accessToken, externalAccountId, externalAccountName, connectedAt);
  }

  /** Reconnecting (e.g. the citizen re-authorizes after a token expired) overwrites the token in
   * place rather than creating a second row — {@code social_connections} has a unique
   * (account_id, platform) constraint for exactly this reason. */
  public void reconnect(String accessToken, String externalAccountId, String externalAccountName, Instant now) {
    this.accessToken = requireNonBlank(accessToken, "accessToken");
    this.externalAccountId = requireNonBlank(externalAccountId, "externalAccountId");
    this.externalAccountName = externalAccountName;
    this.connectedAt = now;
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

  public UUID accountId() {
    return accountId;
  }

  public SocialPlatform platform() {
    return platform;
  }

  public String accessToken() {
    return accessToken;
  }

  public String externalAccountId() {
    return externalAccountId;
  }

  public Optional<String> externalAccountName() {
    return Optional.ofNullable(externalAccountName);
  }

  public Instant connectedAt() {
    return connectedAt;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof SocialConnection other)) return false;
    return id.equals(other.id);
  }

  @Override
  public int hashCode() {
    return id.hashCode();
  }
}
