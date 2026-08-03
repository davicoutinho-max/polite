package dev.civicpulse.identity.domain.model;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * A time-boxed, single-use invite that lets someone self-register as a {@link AccountType#PARTY}
 * or {@link AccountType#POLITICIAN} account instead of the default {@link AccountType#CITIZEN} —
 * replaces the old "admin/party types the new account's password directly" flows (see
 * RegisterPartyService/RegisterPoliticianService in their respective services) so that only the
 * real account holder ever knows their own password. {@code prefillDataJson} is an opaque blob
 * this service never interprets — the issuing service (platform-configuration-service for party
 * tokens, party-management-service for politician tokens) encodes whatever fields it needs
 * (name/acronym/number/... or name/roleTitle/partyId/...) and decodes it back after redemption,
 * which happens there too — redemption consumes the token here but the actual account (and the
 * party/politician-specific rows around it) is created by whichever service redeemed it, the same
 * cross-service sequencing those services already use for their existing direct-registration
 * flows. There is deliberately no {@code consumedByAccountId}: the account doesn't exist yet at
 * the moment of redemption, since the redeeming service creates it immediately afterward. */
public final class RegistrationToken {

  public static final Duration VALIDITY = Duration.ofDays(2);

  private final UUID id;
  private final String token;
  private final AccountType accountType;
  private final UUID issuedByAccountId;
  private final String targetEmail;
  private final String prefillDataJson;
  private final Instant createdAt;
  private Instant expiresAt;
  private Instant consumedAt;
  /** Set when a fresher token superseded this one via {@code resend} — an invalidated token is
   * never valid again even if its {@code expiresAt} hasn't technically passed yet. */
  private boolean invalidated;

  private RegistrationToken(
      UUID id,
      String token,
      AccountType accountType,
      UUID issuedByAccountId,
      String targetEmail,
      String prefillDataJson,
      Instant createdAt,
      Instant expiresAt,
      Instant consumedAt,
      boolean invalidated) {
    this.id = Objects.requireNonNull(id);
    this.token = Objects.requireNonNull(token);
    this.accountType = Objects.requireNonNull(accountType);
    this.issuedByAccountId = Objects.requireNonNull(issuedByAccountId);
    this.targetEmail = targetEmail;
    this.prefillDataJson = prefillDataJson;
    this.createdAt = Objects.requireNonNull(createdAt);
    this.expiresAt = Objects.requireNonNull(expiresAt);
    this.consumedAt = consumedAt;
    this.invalidated = invalidated;
  }

  public static RegistrationToken issue(
      UUID id, String token, AccountType accountType, UUID issuedByAccountId, String targetEmail, String prefillDataJson, Instant now) {
    return new RegistrationToken(id, token, accountType, issuedByAccountId, targetEmail, prefillDataJson, now, now.plus(VALIDITY), null, false);
  }

  public static RegistrationToken reconstitute(
      UUID id,
      String token,
      AccountType accountType,
      UUID issuedByAccountId,
      String targetEmail,
      String prefillDataJson,
      Instant createdAt,
      Instant expiresAt,
      Instant consumedAt,
      boolean invalidated) {
    return new RegistrationToken(id, token, accountType, issuedByAccountId, targetEmail, prefillDataJson, createdAt, expiresAt, consumedAt, invalidated);
  }

  public boolean isValid(Instant now) {
    return !invalidated && consumedAt == null && now.isBefore(expiresAt);
  }

  public void consume(Instant now) {
    this.consumedAt = now;
  }

  /** Burns this token permanently (it can no longer be redeemed even if unexpired) — the caller
   * is responsible for issuing its replacement. */
  public void invalidate() {
    this.invalidated = true;
  }

  public String status(Instant now) {
    if (consumedAt != null) {
      return "consumed";
    }
    if (invalidated || !now.isBefore(expiresAt)) {
      return "expired";
    }
    return "pending";
  }

  public UUID id() {
    return id;
  }

  public String token() {
    return token;
  }

  public AccountType accountType() {
    return accountType;
  }

  public UUID issuedByAccountId() {
    return issuedByAccountId;
  }

  public Optional<String> targetEmail() {
    return Optional.ofNullable(targetEmail);
  }

  public Optional<String> prefillDataJson() {
    return Optional.ofNullable(prefillDataJson);
  }

  public Instant createdAt() {
    return createdAt;
  }

  public Instant expiresAt() {
    return expiresAt;
  }

  public Optional<Instant> consumedAt() {
    return Optional.ofNullable(consumedAt);
  }

  public boolean invalidated() {
    return invalidated;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof RegistrationToken other)) return false;
    return id.equals(other.id);
  }

  @Override
  public int hashCode() {
    return id.hashCode();
  }
}
