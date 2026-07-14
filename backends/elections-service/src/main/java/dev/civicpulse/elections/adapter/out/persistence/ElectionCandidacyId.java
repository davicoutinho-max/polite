package dev.civicpulse.elections.adapter.out.persistence;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

public class ElectionCandidacyId implements Serializable {

  private UUID electionId;
  private UUID politicianAccountId;

  protected ElectionCandidacyId() {}

  public ElectionCandidacyId(UUID electionId, UUID politicianAccountId) {
    this.electionId = electionId;
    this.politicianAccountId = politicianAccountId;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof ElectionCandidacyId other)) return false;
    return Objects.equals(electionId, other.electionId) && Objects.equals(politicianAccountId, other.politicianAccountId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(electionId, politicianAccountId);
  }
}
