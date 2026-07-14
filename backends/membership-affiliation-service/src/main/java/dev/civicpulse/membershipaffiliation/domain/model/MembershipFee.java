package dev.civicpulse.membershipaffiliation.domain.model;

import dev.civicpulse.membershipaffiliation.domain.exception.FeeAlreadyPaidException;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public final class MembershipFee {

  private final UUID id;
  private final UUID affiliationId;
  private final String referencePeriod;
  private final long amountCents;
  private final LocalDate dueDate;
  private FeeStatus status;
  private Instant paidAt;
  private UUID paymentIntentId;

  private MembershipFee(
      UUID id,
      UUID affiliationId,
      String referencePeriod,
      long amountCents,
      LocalDate dueDate,
      FeeStatus status,
      Instant paidAt,
      UUID paymentIntentId) {
    this.id = Objects.requireNonNull(id);
    this.affiliationId = Objects.requireNonNull(affiliationId);
    this.referencePeriod = requireNonBlank(referencePeriod);
    this.amountCents = amountCents;
    this.dueDate = Objects.requireNonNull(dueDate);
    this.status = Objects.requireNonNull(status);
    this.paidAt = paidAt;
    this.paymentIntentId = paymentIntentId;
  }

  public static MembershipFee generate(UUID id, UUID affiliationId, String referencePeriod, long amountCents, LocalDate dueDate) {
    return new MembershipFee(id, affiliationId, referencePeriod, amountCents, dueDate, FeeStatus.PENDING, null, null);
  }

  public static MembershipFee reconstitute(
      UUID id,
      UUID affiliationId,
      String referencePeriod,
      long amountCents,
      LocalDate dueDate,
      FeeStatus status,
      Instant paidAt,
      UUID paymentIntentId) {
    return new MembershipFee(id, affiliationId, referencePeriod, amountCents, dueDate, status, paidAt, paymentIntentId);
  }

  public void markOverdue() {
    if (status == FeeStatus.PENDING) {
      this.status = FeeStatus.OVERDUE;
    }
  }

  public void markPaid(UUID paymentIntentId, Instant now) {
    if (status == FeeStatus.PAID) {
      throw new FeeAlreadyPaidException(id);
    }
    this.status = FeeStatus.PAID;
    this.paidAt = now;
    this.paymentIntentId = paymentIntentId;
  }

  private static String requireNonBlank(String value) {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException("referencePeriod must not be blank");
    }
    return value;
  }

  public UUID id() {
    return id;
  }

  public UUID affiliationId() {
    return affiliationId;
  }

  public String referencePeriod() {
    return referencePeriod;
  }

  public long amountCents() {
    return amountCents;
  }

  public LocalDate dueDate() {
    return dueDate;
  }

  public FeeStatus status() {
    return status;
  }

  public Optional<Instant> paidAt() {
    return Optional.ofNullable(paidAt);
  }

  public Optional<UUID> paymentIntentId() {
    return Optional.ofNullable(paymentIntentId);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof MembershipFee other)) return false;
    return id.equals(other.id);
  }

  @Override
  public int hashCode() {
    return id.hashCode();
  }
}
