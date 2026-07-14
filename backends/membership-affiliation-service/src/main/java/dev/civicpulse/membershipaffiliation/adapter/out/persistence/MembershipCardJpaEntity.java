package dev.civicpulse.membershipaffiliation.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "membership_cards")
public class MembershipCardJpaEntity {

  @Id
  @Column(name = "affiliation_id")
  private UUID affiliationId;

  @Column(name = "member_number", nullable = false, unique = true)
  private String memberNumber;

  @Column(name = "qr_payload", nullable = false)
  private String qrPayload;

  @Column(name = "issued_at", nullable = false)
  private Instant issuedAt;

  protected MembershipCardJpaEntity() {}

  public MembershipCardJpaEntity(UUID affiliationId, String memberNumber, String qrPayload, Instant issuedAt) {
    this.affiliationId = affiliationId;
    this.memberNumber = memberNumber;
    this.qrPayload = qrPayload;
    this.issuedAt = issuedAt;
  }

  public UUID getAffiliationId() {
    return affiliationId;
  }

  public String getMemberNumber() {
    return memberNumber;
  }

  public String getQrPayload() {
    return qrPayload;
  }

  public Instant getIssuedAt() {
    return issuedAt;
  }
}
