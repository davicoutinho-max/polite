package dev.civicpulse.identity.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/** Refresh-token tracking so sessions are revocable (logout-everywhere, compromised-token
 * response) — see {@code sessions} in identity-service/schema.sql. */
public final class Session {

  private final UUID id;
  private final AccountId accountId;
  private final String refreshTokenHash;
  private final String userAgent;
  private final String ipAddress;
  private final Instant issuedAt;
  private final Instant expiresAt;
  private Instant revokedAt;

  private Session(
      UUID id,
      AccountId accountId,
      String refreshTokenHash,
      String userAgent,
      String ipAddress,
      Instant issuedAt,
      Instant expiresAt,
      Instant revokedAt) {
    this.id = Objects.requireNonNull(id);
    this.accountId = Objects.requireNonNull(accountId);
    this.refreshTokenHash = Objects.requireNonNull(refreshTokenHash);
    this.userAgent = userAgent;
    this.ipAddress = ipAddress;
    this.issuedAt = Objects.requireNonNull(issuedAt);
    this.expiresAt = Objects.requireNonNull(expiresAt);
    this.revokedAt = revokedAt;
    if (expiresAt.isBefore(issuedAt)) {
      throw new IllegalArgumentException("expiresAt must be after issuedAt");
    }
  }

  public static Session issue(
      AccountId accountId,
      String refreshTokenHash,
      String userAgent,
      String ipAddress,
      Instant now,
      Instant expiresAt) {
    return new Session(UUID.randomUUID(), accountId, refreshTokenHash, userAgent, ipAddress, now, expiresAt, null);
  }

  public static Session reconstitute(
      UUID id,
      AccountId accountId,
      String refreshTokenHash,
      String userAgent,
      String ipAddress,
      Instant issuedAt,
      Instant expiresAt,
      Instant revokedAt) {
    return new Session(id, accountId, refreshTokenHash, userAgent, ipAddress, issuedAt, expiresAt, revokedAt);
  }

  public boolean isActive(Instant now) {
    return revokedAt == null && now.isBefore(expiresAt);
  }

  public void revoke(Instant now) {
    if (this.revokedAt == null) {
      this.revokedAt = now;
    }
  }

  public UUID id() {
    return id;
  }

  public AccountId accountId() {
    return accountId;
  }

  public String refreshTokenHash() {
    return refreshTokenHash;
  }

  public Optional<String> userAgent() {
    return Optional.ofNullable(userAgent);
  }

  public Optional<String> ipAddress() {
    return Optional.ofNullable(ipAddress);
  }

  public Instant issuedAt() {
    return issuedAt;
  }

  public Instant expiresAt() {
    return expiresAt;
  }

  public Optional<Instant> revokedAt() {
    return Optional.ofNullable(revokedAt);
  }
}
