package dev.civicpulse.identity.adapter.out.persistence;

import dev.civicpulse.identity.domain.model.DocumentStatus;
import dev.civicpulse.identity.domain.model.DocumentType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "document_verification_attempts")
public class DocumentVerificationAttemptJpaEntity {

  @Id private UUID id;

  @Column(name = "account_id", nullable = false)
  private UUID accountId;

  @Column(name = "document_type", nullable = false)
  private DocumentType documentType;

  @Column(nullable = false)
  private DocumentStatus status;

  @Column(nullable = false)
  private String provider;

  @Column(name = "provider_ref")
  private String providerRef;

  @Column(name = "checked_at", nullable = false)
  private Instant checkedAt;

  protected DocumentVerificationAttemptJpaEntity() {}

  public DocumentVerificationAttemptJpaEntity(
      UUID id, UUID accountId, DocumentType documentType, DocumentStatus status, String provider, String providerRef, Instant checkedAt) {
    this.id = id;
    this.accountId = accountId;
    this.documentType = documentType;
    this.status = status;
    this.provider = provider;
    this.providerRef = providerRef;
    this.checkedAt = checkedAt;
  }

  public UUID getId() {
    return id;
  }

  public UUID getAccountId() {
    return accountId;
  }

  public DocumentType getDocumentType() {
    return documentType;
  }

  public DocumentStatus getStatus() {
    return status;
  }

  public String getProvider() {
    return provider;
  }

  public String getProviderRef() {
    return providerRef;
  }

  public Instant getCheckedAt() {
    return checkedAt;
  }
}
