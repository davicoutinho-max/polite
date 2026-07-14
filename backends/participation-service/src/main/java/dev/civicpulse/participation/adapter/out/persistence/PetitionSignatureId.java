package dev.civicpulse.participation.adapter.out.persistence;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

public class PetitionSignatureId implements Serializable {

  private UUID petitionId;
  private UUID citizenAccountId;

  protected PetitionSignatureId() {}

  public PetitionSignatureId(UUID petitionId, UUID citizenAccountId) {
    this.petitionId = petitionId;
    this.citizenAccountId = citizenAccountId;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof PetitionSignatureId other)) return false;
    return Objects.equals(petitionId, other.petitionId) && Objects.equals(citizenAccountId, other.citizenAccountId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(petitionId, citizenAccountId);
  }
}
