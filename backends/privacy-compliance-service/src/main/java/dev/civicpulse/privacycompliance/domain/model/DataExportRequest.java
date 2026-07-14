package dev.civicpulse.privacycompliance.domain.model;

import dev.civicpulse.privacycompliance.domain.exception.InvalidExportTransitionException;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/** No framework imports — the domain core of the hexagonal architecture (see
 * docs/architecture/system-architecture.html). */
public final class DataExportRequest {

  private final UUID id;
  private final UUID accountId;
  private ExportStatus status;
  private final Instant requestedAt;
  private Instant completedAt;
  private String downloadUrl;
  private Instant expiresAt;

  private DataExportRequest(
      UUID id, UUID accountId, ExportStatus status, Instant requestedAt, Instant completedAt, String downloadUrl, Instant expiresAt) {
    this.id = Objects.requireNonNull(id);
    this.accountId = Objects.requireNonNull(accountId);
    this.status = Objects.requireNonNull(status);
    this.requestedAt = Objects.requireNonNull(requestedAt);
    this.completedAt = completedAt;
    this.downloadUrl = downloadUrl;
    this.expiresAt = expiresAt;
  }

  public static DataExportRequest request(UUID id, UUID accountId, Instant now) {
    return new DataExportRequest(id, accountId, ExportStatus.PENDING, now, null, null, null);
  }

  public static DataExportRequest reconstitute(
      UUID id, UUID accountId, ExportStatus status, Instant requestedAt, Instant completedAt, String downloadUrl, Instant expiresAt) {
    return new DataExportRequest(id, accountId, status, requestedAt, completedAt, downloadUrl, expiresAt);
  }

  public void startProcessing() {
    requireStatus(ExportStatus.PENDING, ExportStatus.PROCESSING);
    this.status = ExportStatus.PROCESSING;
  }

  public void markReady(String downloadUrl, Instant expiresAt, Instant now) {
    requireStatus(ExportStatus.PROCESSING, ExportStatus.READY);
    this.status = ExportStatus.READY;
    this.downloadUrl = downloadUrl;
    this.expiresAt = expiresAt;
    this.completedAt = now;
  }

  public void markFailed(Instant now) {
    if (status != ExportStatus.PENDING && status != ExportStatus.PROCESSING) {
      throw new InvalidExportTransitionException(status, ExportStatus.FAILED);
    }
    this.status = ExportStatus.FAILED;
    this.completedAt = now;
  }

  private void requireStatus(ExportStatus expected, ExportStatus target) {
    if (status != expected) {
      throw new InvalidExportTransitionException(status, target);
    }
  }

  public UUID id() {
    return id;
  }

  public UUID accountId() {
    return accountId;
  }

  public ExportStatus status() {
    return status;
  }

  public Instant requestedAt() {
    return requestedAt;
  }

  public Optional<Instant> completedAt() {
    return Optional.ofNullable(completedAt);
  }

  public Optional<String> downloadUrl() {
    return Optional.ofNullable(downloadUrl);
  }

  public Optional<Instant> expiresAt() {
    return Optional.ofNullable(expiresAt);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof DataExportRequest other)) return false;
    return id.equals(other.id);
  }

  @Override
  public int hashCode() {
    return id.hashCode();
  }
}
