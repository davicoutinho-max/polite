package dev.civicpulse.membershipaffiliation.adapter.out.persistence;

import dev.civicpulse.membershipaffiliation.domain.model.FeeStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "membership_fees")
public class MembershipFeeJpaEntity {

  @Id private UUID id;

  @Column(name = "affiliation_id", nullable = false)
  private UUID affiliationId;

  @Column(name = "reference_period", nullable = false)
  private String referencePeriod;

  @Column(name = "amount_cents", nullable = false)
  private long amountCents;

  @Column(name = "due_date", nullable = false)
  private LocalDate dueDate;

  @Column(nullable = false)
  private FeeStatus status;

  @Column(name = "paid_at")
  private Instant paidAt;

  @Column(name = "payment_intent_id")
  private UUID paymentIntentId;

  protected MembershipFeeJpaEntity() {}

  public MembershipFeeJpaEntity(
      UUID id,
      UUID affiliationId,
      String referencePeriod,
      long amountCents,
      LocalDate dueDate,
      FeeStatus status,
      Instant paidAt,
      UUID paymentIntentId) {
    this.id = id;
    this.affiliationId = affiliationId;
    this.referencePeriod = referencePeriod;
    this.amountCents = amountCents;
    this.dueDate = dueDate;
    this.status = status;
    this.paidAt = paidAt;
    this.paymentIntentId = paymentIntentId;
  }

  public UUID getId() {
    return id;
  }

  public UUID getAffiliationId() {
    return affiliationId;
  }

  public String getReferencePeriod() {
    return referencePeriod;
  }

  public long getAmountCents() {
    return amountCents;
  }

  public LocalDate getDueDate() {
    return dueDate;
  }

  public FeeStatus getStatus() {
    return status;
  }

  public Instant getPaidAt() {
    return paidAt;
  }

  public UUID getPaymentIntentId() {
    return paymentIntentId;
  }
}
