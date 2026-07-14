package dev.civicpulse.identity.adapter.out.persistence;

import dev.civicpulse.identity.domain.model.AccountType;
import dev.civicpulse.identity.domain.model.DocumentType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.JdbcType;

/** Maps 1:1 to the {@code accounts} table created by V1__init_schema.sql (the same DDL
 * validated in docs/db/identity-service/schema.sql — no drift between the two). This is a
 * plain persistence record; the domain's {@code Account} aggregate is mapped to/from it by
 * {@link AccountMapper}, never used directly outside this package. */
@Entity
@Table(name = "accounts")
public class AccountJpaEntity {

  @Id private UUID id;

  @Column(name = "account_type", nullable = false)
  private AccountType accountType;

  @Column(nullable = false)
  private String name;

  // handle/email are `citext` in schema.sql (case-insensitive uniqueness) — the JDBC driver
  // reports these as Types#OTHER, not the VARCHAR Hibernate infers from String by default.
  @Column(nullable = false, unique = true)
  @JdbcType(CitextJdbcType.class)
  private String handle;

  @Column(nullable = false, unique = true)
  @JdbcType(CitextJdbcType.class)
  private String email;

  @Column(name = "password_hash", nullable = false)
  private String passwordHash;

  @Column(name = "document_type")
  private DocumentType documentType;

  @Column(name = "document_number_hash", unique = true)
  private String documentNumberHash;

  @Column(name = "document_number_encrypted")
  private byte[] documentNumberEncrypted;

  @Column(nullable = false)
  private boolean verified;

  @Column(name = "anonymized_at")
  private Instant anonymizedAt;

  @Column(name = "avatar_url")
  private String avatarUrl;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  protected AccountJpaEntity() {
    // required by Hibernate
  }

  public AccountJpaEntity(
      UUID id,
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
    this.id = id;
    this.accountType = accountType;
    this.name = name;
    this.handle = handle;
    this.email = email;
    this.passwordHash = passwordHash;
    this.documentType = documentType;
    this.documentNumberHash = documentNumberHash;
    this.documentNumberEncrypted = documentNumberEncrypted;
    this.verified = verified;
    this.anonymizedAt = anonymizedAt;
    this.avatarUrl = avatarUrl;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
  }

  public UUID getId() {
    return id;
  }

  public AccountType getAccountType() {
    return accountType;
  }

  public String getName() {
    return name;
  }

  public String getHandle() {
    return handle;
  }

  public String getEmail() {
    return email;
  }

  public String getPasswordHash() {
    return passwordHash;
  }

  public DocumentType getDocumentType() {
    return documentType;
  }

  public String getDocumentNumberHash() {
    return documentNumberHash;
  }

  public byte[] getDocumentNumberEncrypted() {
    return documentNumberEncrypted;
  }

  public boolean isVerified() {
    return verified;
  }

  public Instant getAnonymizedAt() {
    return anonymizedAt;
  }

  public String getAvatarUrl() {
    return avatarUrl;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }
}
