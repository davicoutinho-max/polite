package dev.civicpulse.payments.adapter.out.persistence;

import dev.civicpulse.payments.domain.model.PaymentGatewayType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "payment_methods")
public class PaymentMethodJpaEntity {

  @Id private UUID id;

  @Column(name = "account_id", nullable = false)
  private UUID accountId;

  @Column(nullable = false)
  private PaymentGatewayType type;

  @Column(name = "token_ref", nullable = false)
  private String tokenRef;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  protected PaymentMethodJpaEntity() {}

  public PaymentMethodJpaEntity(UUID id, UUID accountId, PaymentGatewayType type, String tokenRef, Instant createdAt) {
    this.id = id;
    this.accountId = accountId;
    this.type = type;
    this.tokenRef = tokenRef;
    this.createdAt = createdAt;
  }

  public UUID getId() {
    return id;
  }

  public UUID getAccountId() {
    return accountId;
  }

  public PaymentGatewayType getType() {
    return type;
  }

  public String getTokenRef() {
    return tokenRef;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }
}
