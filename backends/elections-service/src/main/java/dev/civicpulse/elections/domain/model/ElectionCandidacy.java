package dev.civicpulse.elections.domain.model;

import java.util.Objects;
import java.util.UUID;

/** A politician's candidacy in an election — no other attributes; the politician's profile
 * details are resolved against Directory Service at query time, never replicated here (see
 * schema.sql's comment on this table). */
public final class ElectionCandidacy {

  private final UUID electionId;
  private final UUID politicianAccountId;

  private ElectionCandidacy(UUID electionId, UUID politicianAccountId) {
    this.electionId = Objects.requireNonNull(electionId);
    this.politicianAccountId = Objects.requireNonNull(politicianAccountId);
  }

  public static ElectionCandidacy nominate(UUID electionId, UUID politicianAccountId) {
    return new ElectionCandidacy(electionId, politicianAccountId);
  }

  public UUID electionId() {
    return electionId;
  }

  public UUID politicianAccountId() {
    return politicianAccountId;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof ElectionCandidacy other)) return false;
    return electionId.equals(other.electionId) && politicianAccountId.equals(other.politicianAccountId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(electionId, politicianAccountId);
  }
}
