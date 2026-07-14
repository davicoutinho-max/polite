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
