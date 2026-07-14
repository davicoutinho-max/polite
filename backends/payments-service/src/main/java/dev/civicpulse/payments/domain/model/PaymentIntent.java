package dev.civicpulse.payments.domain.model;

import dev.civicpulse.payments.domain.exception.InvalidPaymentTransitionException;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/** One row per attempted charge, shared by every money-movement flow on the platform (Membership
 * fee payment, Fundraising contribution). No framework imports — the domain core of the
 * hexagonal architecture (see docs/architecture/system-architecture.html). */
public final class PaymentIntent {

  private final UUID id;
  private final PaymentPurpose purpose;
  private final UUID referenceId;
  private final UUID payerAccountId;
  private final UUID payeeId;
  private final long amountCents;
  private final String currency;
  private PaymentStatus status;
  private final PaymentGatewayType gateway;
  private String gatewayRef;
  private final String idempotencyKey;
  private final Instant createdAt;
  private Instant updatedAt;

  private PaymentIntent(
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
    this.id = Objects.requireNonNull(id);
    this.purpose = Objects.requireNonNull(purpose);
    this.referenceId = Objects.requireNonNull(referenceId);
    this.payerAccountId = Objects.requireNonNull(payerAccountId);
    this.payeeId = Objects.requireNonNull(payeeId);
    if (amountCents <= 0) {
      throw new IllegalArgumentException("amountCents must be positive");
    }
    this.amountCents = amountCents;
    this.currency = requireNonBlank(currency, "currency");
    this.status = Objects.requireNonNull(status);
    this.gateway = Objects.requireNonNull(gateway);
    this.gatewayRef = gatewayRef;
    this.idempotencyKey = requireNonBlank(idempotencyKey, "idempotencyKey");
    this.createdAt = Objects.requireNonNull(createdAt);
    this.updatedAt = Objects.requireNonNull(updatedAt);
  }

  public static PaymentIntent create(
      UUID id,
      PaymentPurpose purpose,
      UUID referenceId,
      UUID payerAccountId,
      UUID payeeId,
      long amountCents,
      String currency,
      PaymentGatewayType gateway,
      String idempotencyKey,
      Instant now) {
    return new PaymentIntent(
        id, purpose, referenceId, payerAccountId, payeeId, amountCents, currency, PaymentStatus.CREATED, gateway, null, idempotencyKey, now, now);
  }

  public static PaymentIntent reconstitute(
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
    return new PaymentIntent(
        id, purpose, referenceId, payerAccountId, payeeId, amountCents, currency, status, gateway, gatewayRef, idempotencyKey, createdAt, updatedAt);
  }

  public void authorize(String gatewayRef, Instant now) {
    requireStatus(PaymentStatus.CREATED, PaymentStatus.AUTHORIZED);
    this.gatewayRef = gatewayRef;
    this.status = PaymentStatus.AUTHORIZED;
    this.updatedAt = now;
  }

  public void capture(Instant now) {
    requireStatus(PaymentStatus.AUTHORIZED, PaymentStatus.CAPTURED);
    this.status = PaymentStatus.CAPTURED;
    this.updatedAt = now;
  }

  public void fail(Instant now) {
    if (status != PaymentStatus.CREATED && status != PaymentStatus.AUTHORIZED) {
      throw new InvalidPaymentTransitionException(status, PaymentStatus.FAILED);
    }
    this.status = PaymentStatus.FAILED;
    this.updatedAt = now;
  }

  public void refund(Instant now) {
    requireStatus(PaymentStatus.CAPTURED, PaymentStatus.REFUNDED);
    this.status = PaymentStatus.REFUNDED;
    this.updatedAt = now;
  }

  public void cancel(Instant now) {
    requireStatus(PaymentStatus.CREATED, PaymentStatus.CANCELED);
    this.status = PaymentStatus.CANCELED;
    this.updatedAt = now;
  }

  private void requireStatus(PaymentStatus expected, PaymentStatus target) {
    if (status != expected) {
      throw new InvalidPaymentTransitionException(status, target);
    }
  }

  private static String requireNonBlank(String value, String field) {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException(field + " must not be blank");
    }
    return value;
  }

  public UUID id() {
    return id;
  }

  public PaymentPurpose purpose() {
    return purpose;
  }

  public UUID referenceId() {
    return referenceId;
  }

  public UUID payerAccountId() {
    return payerAccountId;
  }

  public UUID payeeId() {
    return payeeId;
  }

  public long amountCents() {
    return amountCents;
  }

  public String currency() {
    return currency;
  }

  public PaymentStatus status() {
    return status;
  }

  public PaymentGatewayType gateway() {
    return gateway;
  }

  public Optional<String> gatewayRef() {
    return Optional.ofNullable(gatewayRef);
  }

  public String idempotencyKey() {
    return idempotencyKey;
  }

  public Instant createdAt() {
    return createdAt;
  }

  public Instant updatedAt() {
    return updatedAt;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof PaymentIntent other)) return false;
    return id.equals(other.id);
  }

  @Override
  public int hashCode() {
    return id.hashCode();
  }
}
