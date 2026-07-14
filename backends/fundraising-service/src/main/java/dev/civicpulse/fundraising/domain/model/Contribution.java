package dev.civicpulse.fundraising.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/** Only ever created after payments-service confirms the money was actually captured — see
 * FundraisingServiceApplication's note. No framework imports — the domain core of the hexagonal
 * architecture (see docs/architecture/system-architecture.html). */
public final class Contribution {

  private final UUID id;
  private final UUID fundraiserId;
  private final UUID supporterAccountId;
  private final long amountCents;
  private final UUID paymentIntentId;
  private final Instant createdAt;

  private Contribution(UUID id, UUID fundraiserId, UUID supporterAccountId, long amountCents, UUID paymentIntentId, Instant createdAt) {
    this.id = Objects.requireNonNull(id);
    this.fundraiserId = Objects.requireNonNull(fundraiserId);
    this.supporterAccountId = Objects.requireNonNull(supporterAccountId);
    if (amountCents <= 0) {
      throw new IllegalArgumentException("amountCents must be positive");
    }
    this.amountCents = amountCents;
    this.paymentIntentId = Objects.requireNonNull(paymentIntentId);
    this.createdAt = Objects.requireNonNull(createdAt);
  }

  public static Contribution record(UUID fundraiserId, UUID supporterAccountId, long amountCents, UUID paymentIntentId, Instant now) {
    return new Contribution(UUID.randomUUID(), fundraiserId, supporterAccountId, amountCents, paymentIntentId, now);
  }

  public static Contribution reconstitute(
      UUID id, UUID fundraiserId, UUID supporterAccountId, long amountCents, UUID paymentIntentId, Instant createdAt) {
    return new Contribution(id, fundraiserId, supporterAccountId, amountCents, paymentIntentId, createdAt);
  }

  public UUID id() {
    return id;
  }

  public UUID fundraiserId() {
    return fundraiserId;
  }

  public UUID supporterAccountId() {
    return supporterAccountId;
  }

  public long amountCents() {
    return amountCents;
  }

  public UUID paymentIntentId() {
    return paymentIntentId;
  }

  public Instant createdAt() {
    return createdAt;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Contribution other)) return false;
    return id.equals(other.id);
  }

  @Override
  public int hashCode() {
    return id.hashCode();
  }
}
