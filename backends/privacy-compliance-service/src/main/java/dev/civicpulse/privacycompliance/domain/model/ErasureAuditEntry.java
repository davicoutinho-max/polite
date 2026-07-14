package dev.civicpulse.privacycompliance.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/** One row per microservice that has erased/anonymized its own copy of an account's data for a
 * given {@link AccountDeletionRequest} — the saga is complete only once every expected service
 * has reported (see {@code ExpectedErasureServices} and schema.sql's comment on this table). */
public final class ErasureAuditEntry {

  private final Long id;
  private final UUID deletionRequestId;
  private final String serviceName;
  private final Instant erasedAt;
  private final Integer recordCount;

  private ErasureAuditEntry(Long id, UUID deletionRequestId, String serviceName, Instant erasedAt, Integer recordCount) {
    this.id = id;
    this.deletionRequestId = Objects.requireNonNull(deletionRequestId);
    this.serviceName = requireNonBlank(serviceName);
    this.erasedAt = Objects.requireNonNull(erasedAt);
    this.recordCount = recordCount;
  }

  public static ErasureAuditEntry record(UUID deletionRequestId, String serviceName, Integer recordCount, Instant now) {
    return new ErasureAuditEntry(null, deletionRequestId, serviceName, now, recordCount);
  }

  public static ErasureAuditEntry reconstitute(Long id, UUID deletionRequestId, String serviceName, Instant erasedAt, Integer recordCount) {
    return new ErasureAuditEntry(id, deletionRequestId, serviceName, erasedAt, recordCount);
  }

  private static String requireNonBlank(String value) {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException("serviceName must not be blank");
    }
    return value;
  }

  public Optional<Long> id() {
    return Optional.ofNullable(id);
  }

  public UUID deletionRequestId() {
    return deletionRequestId;
  }

  public String serviceName() {
    return serviceName;
  }

  public Instant erasedAt() {
    return erasedAt;
  }

  public Optional<Integer> recordCount() {
    return Optional.ofNullable(recordCount);
  }
}
