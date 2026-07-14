package dev.civicpulse.participation.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "petition_signatures")
@IdClass(PetitionSignatureId.class)
public class PetitionSignatureJpaEntity {

  @Id
  @Column(name = "petition_id")
  private UUID petitionId;

  @Id
  @Column(name = "citizen_account_id")
  private UUID citizenAccountId;

  @Column(name = "signed_at", nullable = false)
  private Instant signedAt;

  protected PetitionSignatureJpaEntity() {}

  public PetitionSignatureJpaEntity(UUID petitionId, UUID citizenAccountId, Instant signedAt) {
    this.petitionId = petitionId;
    this.citizenAccountId = citizenAccountId;
    this.signedAt = signedAt;
  }

  public UUID getPetitionId() {
    return petitionId;
  }

  public UUID getCitizenAccountId() {
    return citizenAccountId;
  }

  public Instant getSignedAt() {
    return signedAt;
  }
}
