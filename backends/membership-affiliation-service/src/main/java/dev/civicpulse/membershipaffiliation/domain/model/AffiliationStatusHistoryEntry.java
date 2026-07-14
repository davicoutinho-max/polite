package dev.civicpulse.membershipaffiliation.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/** Full audit trail of the saga — required given two external authorities (party, Electoral
 * Justice) are involved. Append-only. */
public final class AffiliationStatusHistoryEntry {

  private final UUID id;
  private final UUID affiliationId;
  private final AffiliationStatus fromStatus;
  private final AffiliationStatus toStatus;
  private final ChangedBy changedBy;
  private final Instant changedAt;

  private AffiliationStatusHistoryEntry(
      UUID id, UUID affiliationId, AffiliationStatus fromStatus, AffiliationStatus toStatus, ChangedBy changedBy, Instant changedAt) {
    this.id = Objects.requireNonNull(id);
    this.affiliationId = Objects.requireNonNull(affiliationId);
    this.fromStatus = fromStatus;
    this.toStatus = Objects.requireNonNull(toStatus);
    this.changedBy = Objects.requireNonNull(changedBy);
    this.changedAt = Objects.requireNonNull(changedAt);
  }

  public static AffiliationStatusHistoryEntry record(
      UUID id, UUID affiliationId, AffiliationStatus fromStatus, AffiliationStatus toStatus, ChangedBy changedBy, Instant now) {
    return new AffiliationStatusHistoryEntry(id, affiliationId, fromStatus, toStatus, changedBy, now);
  }

  public static AffiliationStatusHistoryEntry reconstitute(
      UUID id, UUID affiliationId, AffiliationStatus fromStatus, AffiliationStatus toStatus, ChangedBy changedBy, Instant changedAt) {
    return new AffiliationStatusHistoryEntry(id, affiliationId, fromStatus, toStatus, changedBy, changedAt);
  }

  public UUID id() {
    return id;
  }

  public UUID affiliationId() {
    return affiliationId;
  }

  public Optional<AffiliationStatus> fromStatus() {
    return Optional.ofNullable(fromStatus);
  }

  public AffiliationStatus toStatus() {
    return toStatus;
  }

  public ChangedBy changedBy() {
    return changedBy;
  }

  public Instant changedAt() {
    return changedAt;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof AffiliationStatusHistoryEntry other)) return false;
    return id.equals(other.id);
  }

  @Override
  public int hashCode() {
    return id.hashCode();
  }
}
