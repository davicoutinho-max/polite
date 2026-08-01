package dev.civicpulse.membershipaffiliation.adapter.out.persistence;

import dev.civicpulse.membershipaffiliation.domain.model.AffiliationStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "affiliations")
public class AffiliationJpaEntity {

  @Id private UUID id;

  @Column(name = "citizen_account_id", nullable = false)
  private UUID citizenAccountId;

  @Column(name = "party_id", nullable = false)
  private UUID partyId;

  @Column(nullable = false)
  private AffiliationStatus status;

  @Column(name = "requested_at")
  private Instant requestedAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  @Column(name = "voter_registration_number")
  private String voterRegistrationNumber;

  @Column(name = "electoral_zone")
  private String electoralZone;

  @Column(name = "electoral_section")
  private String electoralSection;

  @Column(name = "electoral_state")
  private String electoralState;

  @Column(name = "electoral_municipality")
  private String electoralMunicipality;

  @Column(name = "identity_photo_url")
  private String identityPhotoUrl;

  protected AffiliationJpaEntity() {}

  public AffiliationJpaEntity(
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
    this.id = id;
    this.citizenAccountId = citizenAccountId;
    this.partyId = partyId;
    this.status = status;
    this.requestedAt = requestedAt;
    this.updatedAt = updatedAt;
    this.voterRegistrationNumber = voterRegistrationNumber;
    this.electoralZone = electoralZone;
    this.electoralSection = electoralSection;
    this.electoralState = electoralState;
    this.electoralMunicipality = electoralMunicipality;
    this.identityPhotoUrl = identityPhotoUrl;
  }

  public UUID getId() {
    return id;
  }

  public UUID getCitizenAccountId() {
    return citizenAccountId;
  }

  public UUID getPartyId() {
    return partyId;
  }

  public AffiliationStatus getStatus() {
    return status;
  }

  public Instant getRequestedAt() {
    return requestedAt;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }

  public String getVoterRegistrationNumber() {
    return voterRegistrationNumber;
  }

  public String getElectoralZone() {
    return electoralZone;
  }

  public String getElectoralSection() {
    return electoralSection;
  }

  public String getElectoralState() {
    return electoralState;
  }

  public String getElectoralMunicipality() {
    return electoralMunicipality;
  }

  public String getIdentityPhotoUrl() {
    return identityPhotoUrl;
  }
}
