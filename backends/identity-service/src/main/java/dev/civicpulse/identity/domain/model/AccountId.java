package dev.civicpulse.identity.domain.model;

import java.util.Objects;
import java.util.UUID;

/** Identity of an {@link Account}. Wrapping the raw UUID keeps "an account id" from being
 * accidentally interchangeable with any other UUID-typed id in method signatures. */
public final class AccountId {

  private final UUID value;

  private AccountId(UUID value) {
    this.value = Objects.requireNonNull(value, "value must not be null");
  }

  public static AccountId of(UUID value) {
    return new AccountId(value);
  }

  public static AccountId of(String value) {
    return new AccountId(UUID.fromString(value));
  }

  public static AccountId generate() {
    return new AccountId(UUID.randomUUID());
  }

  public UUID value() {
    return value;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof AccountId other)) return false;
    return value.equals(other.value);
  }

  @Override
  public int hashCode() {
    return value.hashCode();
  }

  @Override
  public String toString() {
    return value.toString();
  }
}
