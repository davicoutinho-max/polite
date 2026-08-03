package dev.civicpulse.identity.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "registration_tokens")
public class RegistrationTokenJpaEntity {

  @Id
  private UUID id;

  @Column(nullable = false, unique = true)
  private String token;

  @Column(name = "account_type", nullable = false)
  private String accountType;

  @Column(name = "issued_by_account_id", nullable = false)
  private UUID issuedByAccountId;

  @Column(name = "target_email")
  private String targetEmail;

  @Column(name = "prefill_data_json", columnDefinition = "text")
  private String prefillDataJson;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @Column(name = "expires_at", nullable = false)
  private Instant expiresAt;

  @Column(name = "consumed_at")
  private Instant consumedAt;

  @Column(nullable = false)
  private boolean invalidated;

  protected RegistrationTokenJpaEntity() {}

  public RegistrationTokenJpaEntity(
      UUID id,
      String token,
      String accountType,
      UUID issuedByAccountId,
      String targetEmail,
      String prefillDataJson,
      Instant createdAt,
      Instant expiresAt,
      Instant consumedAt,
      boolean invalidated) {
    this.id = id;
    this.token = token;
    this.accountType = accountType;
    this.issuedByAccountId = issuedByAccountId;
    this.targetEmail = targetEmail;
    this.prefillDataJson = prefillDataJson;
    this.createdAt = createdAt;
    this.expiresAt = expiresAt;
    this.consumedAt = consumedAt;
    this.invalidated = invalidated;
  }

  public UUID getId() {
    return id;
  }

  public String getToken() {
    return token;
  }

  public String getAccountType() {
    return accountType;
  }

  public UUID getIssuedByAccountId() {
    return issuedByAccountId;
  }

  public String getTargetEmail() {
    return targetEmail;
  }

  public String getPrefillDataJson() {
    return prefillDataJson;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public Instant getExpiresAt() {
    return expiresAt;
  }

  public Instant getConsumedAt() {
    return consumedAt;
  }

  public boolean isInvalidated() {
    return invalidated;
  }
}
