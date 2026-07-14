package dev.civicpulse.partymanagement.domain.model;

import dev.civicpulse.partymanagement.domain.exception.AffiliationRequestNotPendingException;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/** Party-side review step of the Affiliation Lifecycle (flow 04) — approve/reject only; the
 * multi-stage saga itself is owned by Membership &amp; Affiliation (this row is created here by
 * consuming that service's {@code AffiliationRequested} event). */
public final class AffiliationRequest {

  private final UUID id;
  private final UUID partyId;
  private final UUID citizenAccountId;
  private final String city;
  private AffiliationRequestStatus status;
  private final Instant requestedAt;
  private Instant decidedAt;

  private AffiliationRequest(
      UUID id,
      UUID partyId,
      UUID citizenAccountId,
      String city,
      AffiliationRequestStatus status,
      Instant requestedAt,
      Instant decidedAt) {
    this.id = Objects.requireNonNull(id);
    this.partyId = Objects.requireNonNull(partyId);
    this.citizenAccountId = Objects.requireNonNull(citizenAccountId);
    this.city = city;
    this.status = Objects.requireNonNull(status);
    this.requestedAt = Objects.requireNonNull(requestedAt);
    this.decidedAt = decidedAt;
  }

  public static AffiliationRequest create(UUID id, UUID partyId, UUID citizenAccountId, String city, Instant now) {
    return new AffiliationRequest(id, partyId, citizenAccountId, city, AffiliationRequestStatus.PENDING, now, null);
  }

  public static AffiliationRequest reconstitute(
      UUID id,
      UUID partyId,
      UUID citizenAccountId,
      String city,
      AffiliationRequestStatus status,
      Instant requestedAt,
      Instant decidedAt) {
    return new AffiliationRequest(id, partyId, citizenAccountId, city, status, requestedAt, decidedAt);
  }

  public void approve(Instant now) {
    requirePending();
    this.status = AffiliationRequestStatus.APPROVED;
    this.decidedAt = now;
  }

  public void reject(Instant now) {
    requirePending();
    this.status = AffiliationRequestStatus.REJECTED;
    this.decidedAt = now;
  }

  private void requirePending() {
    if (status != AffiliationRequestStatus.PENDING) {
      throw new AffiliationRequestNotPendingException(id, status);
    }
  }

  public UUID id() {
    return id;
  }

  public UUID partyId() {
    return partyId;
  }

  public UUID citizenAccountId() {
    return citizenAccountId;
  }

  public Optional<String> city() {
    return Optional.ofNullable(city);
  }

  public AffiliationRequestStatus status() {
    return status;
  }

  public Instant requestedAt() {
    return requestedAt;
  }

  public Optional<Instant> decidedAt() {
    return Optional.ofNullable(decidedAt);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof AffiliationRequest other)) return false;
    return id.equals(other.id);
  }

  @Override
  public int hashCode() {
    return id.hashCode();
  }
}
