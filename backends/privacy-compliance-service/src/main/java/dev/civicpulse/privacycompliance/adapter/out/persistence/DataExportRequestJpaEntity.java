package dev.civicpulse.privacycompliance.adapter.out.persistence;

import dev.civicpulse.privacycompliance.domain.model.ExportStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "data_export_requests")
public class DataExportRequestJpaEntity {

  @Id private UUID id;

  @Column(name = "account_id", nullable = false)
  private UUID accountId;

  @Column(nullable = false)
  private ExportStatus status;

  @Column(name = "requested_at", nullable = false)
  private Instant requestedAt;

  @Column(name = "completed_at")
  private Instant completedAt;

  @Column(name = "download_url")
  private String downloadUrl;

  @Column(name = "expires_at")
  private Instant expiresAt;

  protected DataExportRequestJpaEntity() {}

  public DataExportRequestJpaEntity(
      UUID id, UUID accountId, ExportStatus status, Instant requestedAt, Instant completedAt, String downloadUrl, Instant expiresAt) {
    this.id = id;
    this.accountId = accountId;
    this.status = status;
    this.requestedAt = requestedAt;
    this.completedAt = completedAt;
    this.downloadUrl = downloadUrl;
    this.expiresAt = expiresAt;
  }

  public UUID getId() {
    return id;
  }

  public UUID getAccountId() {
    return accountId;
  }

  public ExportStatus getStatus() {
    return status;
  }

  public Instant getRequestedAt() {
    return requestedAt;
  }

  public Instant getCompletedAt() {
    return completedAt;
  }

  public String getDownloadUrl() {
    return downloadUrl;
  }

  public Instant getExpiresAt() {
    return expiresAt;
  }
}
