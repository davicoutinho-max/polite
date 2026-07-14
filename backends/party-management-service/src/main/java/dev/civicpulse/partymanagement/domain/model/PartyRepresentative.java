package dev.civicpulse.partymanagement.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/** Politician ↔ party linkage — created either when a party registers a brand-new politician
 * (flow 02) or when Platform Admin reassigns an existing one (flow 03). */
public final class PartyRepresentative {

  private final UUID id;
  private final UUID partyId;
  private final UUID politicianAccountId;
  private final String roleTitle;
  private final Instant linkedAt;

  private PartyRepresentative(UUID id, UUID partyId, UUID politicianAccountId, String roleTitle, Instant linkedAt) {
    this.id = Objects.requireNonNull(id);
    this.partyId = Objects.requireNonNull(partyId);
    this.politicianAccountId = Objects.requireNonNull(politicianAccountId);
    this.roleTitle = roleTitle;
    this.linkedAt = Objects.requireNonNull(linkedAt);
  }

  public static PartyRepresentative link(UUID id, UUID partyId, UUID politicianAccountId, String roleTitle, Instant now) {
    return new PartyRepresentative(id, partyId, politicianAccountId, roleTitle, now);
  }

  public static PartyRepresentative reconstitute(
      UUID id, UUID partyId, UUID politicianAccountId, String roleTitle, Instant linkedAt) {
    return new PartyRepresentative(id, partyId, politicianAccountId, roleTitle, linkedAt);
  }

  public UUID id() {
    return id;
  }

  public UUID partyId() {
    return partyId;
  }

  public UUID politicianAccountId() {
    return politicianAccountId;
  }

  public Optional<String> roleTitle() {
    return Optional.ofNullable(roleTitle);
  }

  public Instant linkedAt() {
    return linkedAt;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof PartyRepresentative other)) return false;
    return id.equals(other.id);
  }

  @Override
  public int hashCode() {
    return id.hashCode();
  }
}
