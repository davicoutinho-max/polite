package dev.civicpulse.fundraising.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "contributions")
public class ContributionJpaEntity {

  @Id private UUID id;

  @Column(name = "fundraiser_id", nullable = false)
  private UUID fundraiserId;

  @Column(name = "supporter_account_id", nullable = false)
  private UUID supporterAccountId;

  @Column(name = "amount_cents", nullable = false)
  private long amountCents;

  @Column(name = "payment_intent_id", nullable = false, unique = true)
  private UUID paymentIntentId;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  protected ContributionJpaEntity() {}

  public ContributionJpaEntity(UUID id, UUID fundraiserId, UUID supporterAccountId, long amountCents, UUID paymentIntentId, Instant createdAt) {
    this.id = id;
    this.fundraiserId = fundraiserId;
    this.supporterAccountId = supporterAccountId;
    this.amountCents = amountCents;
    this.paymentIntentId = paymentIntentId;
    this.createdAt = createdAt;
  }

  public UUID getId() {
    return id;
  }

  public UUID getFundraiserId() {
    return fundraiserId;
  }

  public UUID getSupporterAccountId() {
    return supporterAccountId;
  }

  public long getAmountCents() {
    return amountCents;
  }

  public UUID getPaymentIntentId() {
    return paymentIntentId;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }
}
