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
  /** Título de eleitor — the citizen's voter-registration number. Required by TSE Resolução
   * 23.571/2018 for a valid affiliation request; captured once at request time, never edited. */
  private final String voterRegistrationNumber;
  private final String electoralZone;
  private final String electoralSection;
  /** UF (state) of voter registration — not necessarily the citizen's current city/state. */
  private final String electoralState;
  private final String electoralMunicipality;
  /** A selfie holding the citizen's ID document, per the same TSE resolution's identity-check
   * requirement for the affiliation's electronic signature to be valid. */
  private final String identityPhotoUrl;

  private Affiliation(
      UUID id,
      UUID citizenAccountId,
      UUID partyId,
      AffiliationStatus status,
      Instant requestedAt,
      Instant updatedAt,
      String voterRegistrationNumber,
      String electoralZone,
      String electoralSection,
      String electoralState,
      String electoralMunicipality,
      String identityPhotoUrl) {
    this.id = Objects.requireNonNull(id);
    this.citizenAccountId = Objects.requireNonNull(citizenAccountId);
    this.partyId = Objects.requireNonNull(partyId);
    this.status = Objects.requireNonNull(status);
    this.requestedAt = requestedAt;
    this.updatedAt = Objects.requireNonNull(updatedAt);
    this.voterRegistrationNumber = voterRegistrationNumber;
    this.electoralZone = electoralZone;
    this.electoralSection = electoralSection;
    this.electoralState = electoralState;
    this.electoralMunicipality = electoralMunicipality;
    this.identityPhotoUrl = identityPhotoUrl;
  }

  public static Affiliation request(
      UUID id,
      UUID citizenAccountId,
      UUID partyId,
      String voterRegistrationNumber,
      String electoralZone,
      String electoralSection,
      String electoralState,
      String electoralMunicipality,
      String identityPhotoUrl,
      Instant now) {
    return new Affiliation(
        id, citizenAccountId, partyId, AffiliationStatus.REQUESTED, now, now, voterRegistrationNumber, electoralZone, electoralSection,
        electoralState, electoralMunicipality, identityPhotoUrl);
  }

  public static Affiliation reconstitute(
      UUID id,
      UUID citizenAccountId,
      UUID partyId,
      AffiliationStatus status,
      Instant requestedAt,
      Instant updatedAt,
      String voterRegistrationNumber,
      String electoralZone,
      String electoralSection,
      String electoralState,
      String electoralMunicipality,
      String identityPhotoUrl) {
    return new Affiliation(
        id, citizenAccountId, partyId, status, requestedAt, updatedAt, voterRegistrationNumber, electoralZone, electoralSection, electoralState,
        electoralMunicipality, identityPhotoUrl);
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

  public Optional<String> voterRegistrationNumber() {
    return Optional.ofNullable(voterRegistrationNumber);
  }

  public Optional<String> electoralZone() {
    return Optional.ofNullable(electoralZone);
  }

  public Optional<String> electoralSection() {
    return Optional.ofNullable(electoralSection);
  }

  public Optional<String> electoralState() {
    return Optional.ofNullable(electoralState);
  }

  public Optional<String> electoralMunicipality() {
    return Optional.ofNullable(electoralMunicipality);
  }

  public Optional<String> identityPhotoUrl() {
    return Optional.ofNullable(identityPhotoUrl);
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
