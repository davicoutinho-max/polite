package dev.civicpulse.payments.adapter.out.persistence;

import dev.civicpulse.payments.domain.model.PaymentGatewayType;
import dev.civicpulse.payments.domain.model.PaymentPurpose;
import dev.civicpulse.payments.domain.model.PaymentStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "payment_intents")
public class PaymentIntentJpaEntity {

  @Id private UUID id;

  @Column(nullable = false)
  private PaymentPurpose purpose;

  @Column(name = "reference_id", nullable = false)
  private UUID referenceId;

  @Column(name = "payer_account_id", nullable = false)
  private UUID payerAccountId;

  @Column(name = "payee_id", nullable = false)
  private UUID payeeId;

  @Column(name = "amount_cents", nullable = false)
  private long amountCents;

  // schema.sql declares this char(3) (fixed-length bpchar) — see identity-service's citext
  // fix / platform-configuration-service's countries.code for the same class of mismatch.
  @Column(nullable = false, length = 3)
  @JdbcTypeCode(SqlTypes.CHAR)
  private String currency;

  @Column(nullable = false)
  private PaymentStatus status;

  @Column(nullable = false)
  private PaymentGatewayType gateway;

  @Column(name = "gateway_ref")
  private String gatewayRef;

  @Column(name = "idempotency_key", nullable = false, unique = true)
  private String idempotencyKey;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  protected PaymentIntentJpaEntity() {}

  public PaymentIntentJpaEntity(
      UUID id,
      PaymentPurpose purpose,
      UUID referenceId,
      UUID payerAccountId,
      UUID payeeId,
      long amountCents,
      String currency,
      PaymentStatus status,
      PaymentGatewayType gateway,
      String gatewayRef,
      String idempotencyKey,
      Instant createdAt,
      Instant updatedAt) {
    this.id = id;
    this.purpose = purpose;
    this.referenceId = referenceId;
    this.payerAccountId = payerAccountId;
    this.payeeId = payeeId;
    this.amountCents = amountCents;
    this.currency = currency;
    this.status = status;
    this.gateway = gateway;
    this.gatewayRef = gatewayRef;
    this.idempotencyKey = idempotencyKey;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
  }

  public UUID getId() {
    return id;
  }

  public PaymentPurpose getPurpose() {
    return purpose;
  }

  public UUID getReferenceId() {
    return referenceId;
  }

  public UUID getPayerAccountId() {
    return payerAccountId;
  }

  public UUID getPayeeId() {
    return payeeId;
  }

  public long getAmountCents() {
    return amountCents;
  }

  public String getCurrency() {
    return currency;
  }

  public PaymentStatus getStatus() {
    return status;
  }

  public PaymentGatewayType getGateway() {
    return gateway;
  }

  public String getGatewayRef() {
    return gatewayRef;
  }

  public String getIdempotencyKey() {
    return idempotencyKey;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }
}
