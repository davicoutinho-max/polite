package dev.civicpulse.identity.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

/**
 * One authenticatable identity. Rich profile data (office, party, bio) is owned by
 * Directory/Party Management, not here — see docs/architecture/data-architecture.html.
 *
 * <p>This class has no framework imports on purpose: it is the domain core of the hexagonal
 * architecture documented in docs/architecture/system-architecture.html, and must remain
 * testable with nothing but the JDK.
 */
public final class Account {

  private final AccountId id;
  private final AccountType accountType;
  private String name;
  private final String handle;
  private final String email;
  private String passwordHash;
  private final DocumentType documentType;
  private final String documentNumberHash;
  private final byte[] documentNumberEncrypted;
  private boolean verified;
  private Instant anonymizedAt;
  private String avatarUrl;
  private String externalSource;
  private String externalId;
  private final Instant createdAt;
  private Instant updatedAt;

  private Account(
      AccountId id,
      AccountType accountType,
      String name,
      String handle,
      String email,
      String passwordHash,
      DocumentType documentType,
      String documentNumberHash,
      byte[] documentNumberEncrypted,
      boolean verified,
      Instant anonymizedAt,
      String avatarUrl,
      String externalSource,
      String externalId,
      Instant createdAt,
      Instant updatedAt) {
    this.id = Objects.requireNonNull(id);
    this.accountType = Objects.requireNonNull(accountType);
    this.name = requireNonBlank(name, "name");
    this.handle = requireNonBlank(handle, "handle");
    this.email = requireNonBlank(email, "email");
    this.passwordHash = Objects.requireNonNull(passwordHash, "passwordHash");
    this.documentType = documentType;
    this.documentNumberHash = documentNumberHash;
    this.documentNumberEncrypted = documentNumberEncrypted;
    this.verified = verified;
    this.anonymizedAt = anonymizedAt;
    this.avatarUrl = avatarUrl;
    this.externalSource = externalSource;
    this.externalId = externalId;
    this.createdAt = Objects.requireNonNull(createdAt);
    this.updatedAt = Objects.requireNonNull(updatedAt);

    if (accountType != AccountType.ADMIN) {
      Objects.requireNonNull(documentType, "documentType is required for account_type=" + accountType);
      Objects.requireNonNull(documentNumberHash, "documentNumberHash is required for account_type=" + accountType);
    }
  }

  /** Registers a brand-new account. CPF/CNPJ hashing/encryption already happened in the
   * application layer (via the DocumentCipher port) before this factory runs — the domain
   * never sees a raw document number. */
  public static Account register(
      AccountId id,
      AccountType accountType,
      String name,
      String handle,
      String email,
      String passwordHash,
      DocumentType documentType,
      String documentNumberHash,
      byte[] documentNumberEncrypted,
      Instant now) {
    return new Account(
        id,
        accountType,
        name,
        handle,
        email,
        passwordHash,
        documentType,
        documentNumberHash,
        documentNumberEncrypted,
        false,
        null,
        null,
        null,
        null,
        now,
        now);
  }

  /** Registers an account on behalf of a real person/organization who never signed up —
   * provisioned by a government-data sync job (see government-sync-service), not by the account
   * holder. There is no password to store (reusing {@link #anonymize}'s existing convention of an
   * empty, never-matchable hash rather than relaxing the domain's non-null invariant); login is
   * explicitly refused for any account carrying an {@code externalSource} — see
   * AuthenticateService. {@code externalSource}/{@code externalId} together are the sync job's
   * idempotency key (e.g. "CAMARA_DEPUTADO"/"204379") so re-running it updates instead of
   * duplicating. */
  public static Account registerSynced(
      AccountId id,
      AccountType accountType,
      String name,
      String handle,
      String email,
      DocumentType documentType,
      String documentNumberHash,
      byte[] documentNumberEncrypted,
      String avatarUrl,
      String externalSource,
      String externalId,
      Instant now) {
    return new Account(
        id,
        accountType,
        name,
        handle,
        email,
        "",
        documentType,
        documentNumberHash,
        documentNumberEncrypted,
        false,
        null,
        avatarUrl,
        Objects.requireNonNull(externalSource, "externalSource"),
        Objects.requireNonNull(externalId, "externalId"),
        now,
        now);
  }

  /** Reconstructs an account from persisted state — used only by the persistence adapter's
   * mapper, never by application services directly. */
  public static Account reconstitute(
      AccountId id,
      AccountType accountType,
      String name,
      String handle,
      String email,
      String passwordHash,
      DocumentType documentType,
      String documentNumberHash,
      byte[] documentNumberEncrypted,
      boolean verified,
      Instant anonymizedAt,
      String avatarUrl,
      String externalSource,
      String externalId,
      Instant createdAt,
      Instant updatedAt) {
    return new Account(
        id,
        accountType,
        name,
        handle,
        email,
        passwordHash,
        documentType,
        documentNumberHash,
        documentNumberEncrypted,
        verified,
        anonymizedAt,
        avatarUrl,
        externalSource,
        externalId,
        createdAt,
        updatedAt);
  }

  public void markVerified(Instant now) {
    if (this.verified) {
      return;
    }
    this.verified = true;
    this.updatedAt = now;
  }

  /** LGPD erasure: overwrite PII in place rather than deleting the row — other services still
   * hold this id in posts/comments/messages and must keep resolving it. See
   * docs/architecture/data-architecture.html's "soft-anonymize, don't hard-delete" pattern. */
  public void anonymize(Instant now) {
    if (this.anonymizedAt != null) {
      return;
    }
    this.name = "Deleted account";
    this.passwordHash = "";
    this.avatarUrl = null;
    this.anonymizedAt = now;
    this.updatedAt = now;
  }

  public boolean isAnonymized() {
    return anonymizedAt != null;
  }

  /** True for accounts provisioned by a government-data sync job rather than a real signup —
   * these can never log in (see {@link #registerSynced}). */
  public boolean isSynced() {
    return externalSource != null;
  }

  /** Refreshes the display fields a re-sync might have changed (official photo, name spelling).
   * No-op for anything else — a synced account never gains a password or document through this
   * path. */
  public void updateSyncedProfile(String name, String avatarUrl, Instant now) {
    this.name = requireNonBlank(name, "name");
    this.avatarUrl = avatarUrl;
    this.updatedAt = now;
  }

  /** Grants real login access to a profile that a government-data sync created (see
   * {@link #registerSynced}) — the "someone registers with the same CPF/CNPJ a synced profile was
   * built from" flow: rather than creating a second, duplicate profile for the same real person,
   * {@code RegisterAccountService} attaches credentials to this one instead. Name/handle/email are
   * deliberately left untouched (the government-sourced official identity, not whatever the
   * claimer typed, remains the profile's public data) — this method only ever changes whether the
   * account can log in. Clearing {@code externalSource}/{@code externalId} is what actually flips
   * {@link #isSynced()} to false (the login guard in AuthenticateService keys off that, not off
   * password presence alone) and, as a side effect, makes this record invisible to future sync
   * runs' {@code findByExternalSourceAndExternalId} lookup — deliberately: once claimed, a real
   * owner controls this profile, and the sync job re-provisioning/overwriting their photo or name
   * on its next run would be the wrong behavior, not a bug. */
  public void claim(String passwordHash, Instant now) {
    if (!isSynced()) {
      throw new IllegalStateException("only a synced account can be claimed");
    }
    this.passwordHash = Objects.requireNonNull(passwordHash, "passwordHash");
    this.externalSource = null;
    this.externalId = null;
    this.updatedAt = now;
  }

  private static String requireNonBlank(String value, String field) {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException(field + " must not be blank");
    }
    return value;
  }

  public AccountId id() {
    return id;
  }

  public AccountType accountType() {
    return accountType;
  }

  public String name() {
    return name;
  }

  public String handle() {
    return handle;
  }

  public String email() {
    return email;
  }

  public String passwordHash() {
    return passwordHash;
  }

  public Optional<DocumentType> documentType() {
    return Optional.ofNullable(documentType);
  }

  public Optional<String> documentNumberHash() {
    return Optional.ofNullable(documentNumberHash);
  }

  public Optional<byte[]> documentNumberEncrypted() {
    return Optional.ofNullable(documentNumberEncrypted);
  }

  public boolean verified() {
    return verified;
  }

  public Optional<Instant> anonymizedAt() {
    return Optional.ofNullable(anonymizedAt);
  }

  public Optional<String> avatarUrl() {
    return Optional.ofNullable(avatarUrl);
  }

  public Optional<String> externalSource() {
    return Optional.ofNullable(externalSource);
  }

  public Optional<String> externalId() {
    return Optional.ofNullable(externalId);
  }

  public Instant createdAt() {
    return createdAt;
  }

  public Instant updatedAt() {
    return updatedAt;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Account other)) return false;
    return id.equals(other.id);
  }

  @Override
  public int hashCode() {
    return id.hashCode();
  }
}
