package dev.civicpulse.participation.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public final class PetitionSignature {

  private final UUID petitionId;
  private final UUID citizenAccountId;
  private final Instant signedAt;

  private PetitionSignature(UUID petitionId, UUID citizenAccountId, Instant signedAt) {
    this.petitionId = Objects.requireNonNull(petitionId);
    this.citizenAccountId = Objects.requireNonNull(citizenAccountId);
    this.signedAt = Objects.requireNonNull(signedAt);
  }

  public static PetitionSignature sign(UUID petitionId, UUID citizenAccountId, Instant now) {
    return new PetitionSignature(petitionId, citizenAccountId, now);
  }

  public static PetitionSignature reconstitute(UUID petitionId, UUID citizenAccountId, Instant signedAt) {
    return new PetitionSignature(petitionId, citizenAccountId, signedAt);
  }

  public UUID petitionId() {
    return petitionId;
  }

  public UUID citizenAccountId() {
    return citizenAccountId;
  }

  public Instant signedAt() {
    return signedAt;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof PetitionSignature other)) return false;
    return petitionId.equals(other.petitionId) && citizenAccountId.equals(other.citizenAccountId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(petitionId, citizenAccountId);
  }
}
