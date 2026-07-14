package dev.civicpulse.membershipaffiliation.domain.model;

import dev.civicpulse.membershipaffiliation.domain.exception.InvalidAffiliationTransitionException;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/** One saga instance per citizen-party pair. Status only ever advances forward along {@link
 * AffiliationStatus#sortOrder()}, except for the one-way exit to {@code REJECTED}, which is
 * reachable from any non-terminal status (see affiliation-lifecycle.bpmn). No framework
 * imports — the domain core of the hexagonal architecture (see
 * docs/architecture/system-architecture.html). */
public final class Affiliation {

  private final UUID id;
  private final UUID citizenAccountId;
  private final UUID partyId;
  private AffiliationStatus status;
  private Instant requestedAt;
  private Instant updatedAt;

  private Affiliation(UUID id, UUID citizenAccountId, UUID partyId, AffiliationStatus status, Instant requestedAt, Instant updatedAt) {
    this.id = Objects.requireNonNull(id);
    this.citizenAccountId = Objects.requireNonNull(citizenAccountId);
    this.partyId = Objects.requireNonNull(partyId);
    this.status = Objects.requireNonNull(status);
    this.requestedAt = requestedAt;
    this.updatedAt = Objects.requireNonNull(updatedAt);
  }

  public static Affiliation request(UUID id, UUID citizenAccountId, UUID partyId, Instant now) {
    return new Affiliation(id, citizenAccountId, partyId, AffiliationStatus.REQUESTED, now, now);
  }

  public static Affiliation reconstitute(
      UUID id, UUID citizenAccountId, UUID partyId, AffiliationStatus status, Instant requestedAt, Instant updatedAt) {
    return new Affiliation(id, citizenAccountId, partyId, status, requestedAt, updatedAt);
  }

  public void startReview(Instant now) {
    transitionTo(AffiliationStatus.UNDER_REVIEW, now);
  }

  public void approveByParty(Instant now) {
    transitionTo(AffiliationStatus.PARTY_APPROVED, now);
  }

  public void sendToElectoralJustice(Instant now) {
    transitionTo(AffiliationStatus.ELECTORAL_JUSTICE, now);
  }

  public void confirm(Instant now) {
    transitionTo(AffiliationStatus.AFFILIATED, now);
  }

  public void reject(Instant now) {
    if (status == AffiliationStatus.AFFILIATED || status == AffiliationStatus.REJECTED) {
      throw new InvalidAffiliationTransitionException(status, AffiliationStatus.REJECTED);
    }
    this.status = AffiliationStatus.REJECTED;
    this.updatedAt = now;
  }

  /** Forward-only: the target's sort order must be exactly one step ahead of the current
   * status. Anything else (skipping a step, moving backward, re-entering a terminal status)
   * is rejected. */
  private void transitionTo(AffiliationStatus target, Instant now) {
    if (target.sortOrder() != status.sortOrder() + 1) {
      throw new InvalidAffiliationTransitionException(status, target);
    }
    this.status = target;
    this.updatedAt = now;
  }

  public UUID id() {
    return id;
  }

  public UUID citizenAccountId() {
    return citizenAccountId;
  }

  public UUID partyId() {
    return partyId;
  }

  public AffiliationStatus status() {
    return status;
  }

  public Optional<Instant> requestedAt() {
    return Optional.ofNullable(requestedAt);
  }

  public Instant updatedAt() {
    return updatedAt;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Affiliation other)) return false;
    return id.equals(other.id);
  }

  @Override
  public int hashCode() {
    return id.hashCode();
  }
}
