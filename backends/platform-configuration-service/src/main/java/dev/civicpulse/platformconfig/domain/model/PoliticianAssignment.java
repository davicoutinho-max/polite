package dev.civicpulse.platformconfig.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/** Platform-admin-controlled party assignment, independent from how the politician was
 * originally registered (see docs/db/platform-configuration-service/schema.sql). Created
 * automatically on {@code PoliticianRegistered}, updatable by Platform Admin reassignment. */
public final class PoliticianAssignment {

  private final UUID politicianAccountId;
  private UUID partyId;
  private Instant updatedAt;

  private PoliticianAssignment(UUID politicianAccountId, UUID partyId, Instant updatedAt) {
    this.politicianAccountId = Objects.requireNonNull(politicianAccountId);
    this.partyId = Objects.requireNonNull(partyId);
    this.updatedAt = Objects.requireNonNull(updatedAt);
  }

  public static PoliticianAssignment assign(UUID politicianAccountId, UUID partyId, Instant now) {
    return new PoliticianAssignment(politicianAccountId, partyId, now);
  }

  public static PoliticianAssignment reconstitute(UUID politicianAccountId, UUID partyId, Instant updatedAt) {
    return new PoliticianAssignment(politicianAccountId, partyId, updatedAt);
  }

  public void reassign(UUID newPartyId, Instant now) {
    this.partyId = Objects.requireNonNull(newPartyId);
    this.updatedAt = now;
  }

  public UUID politicianAccountId() {
    return politicianAccountId;
  }

  public UUID partyId() {
    return partyId;
  }

  public Instant updatedAt() {
    return updatedAt;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof PoliticianAssignment other)) return false;
    return politicianAccountId.equals(other.politicianAccountId);
  }

  @Override
  public int hashCode() {
    return politicianAccountId.hashCode();
  }
}
