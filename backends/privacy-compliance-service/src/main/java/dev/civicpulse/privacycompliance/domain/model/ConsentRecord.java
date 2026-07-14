package dev.civicpulse.privacycompliance.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public final class ConsentRecord {

  private final UUID accountId;
  private final ConsentPurpose purpose;
  private boolean granted;
  private Instant updatedAt;

  private ConsentRecord(UUID accountId, ConsentPurpose purpose, boolean granted, Instant updatedAt) {
    this.accountId = Objects.requireNonNull(accountId);
    this.purpose = Objects.requireNonNull(purpose);
    this.granted = granted;
    this.updatedAt = Objects.requireNonNull(updatedAt);
  }

  public static ConsentRecord record(UUID accountId, ConsentPurpose purpose, boolean granted, Instant now) {
    return new ConsentRecord(accountId, purpose, granted, now);
  }

  public static ConsentRecord reconstitute(UUID accountId, ConsentPurpose purpose, boolean granted, Instant updatedAt) {
    return new ConsentRecord(accountId, purpose, granted, updatedAt);
  }

  public void update(boolean granted, Instant now) {
    this.granted = granted;
    this.updatedAt = now;
  }

  public UUID accountId() {
    return accountId;
  }

  public ConsentPurpose purpose() {
    return purpose;
  }

  public boolean granted() {
    return granted;
  }

  public Instant updatedAt() {
    return updatedAt;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof ConsentRecord other)) return false;
    return accountId.equals(other.accountId) && purpose == other.purpose;
  }

  @Override
  public int hashCode() {
    return Objects.hash(accountId, purpose);
  }
}
