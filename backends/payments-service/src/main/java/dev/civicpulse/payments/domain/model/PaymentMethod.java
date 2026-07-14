package dev.civicpulse.payments.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/** Only a vault token is stored — raw card data never touches this database (PCI scope stays
 * with the gateway). */
public final class PaymentMethod {

  private final UUID id;
  private final UUID accountId;
  private final PaymentGatewayType type;
  private final String tokenRef;
  private final Instant createdAt;

  private PaymentMethod(UUID id, UUID accountId, PaymentGatewayType type, String tokenRef, Instant createdAt) {
    this.id = Objects.requireNonNull(id);
    this.accountId = Objects.requireNonNull(accountId);
    this.type = Objects.requireNonNull(type);
    this.tokenRef = requireNonBlank(tokenRef);
    this.createdAt = Objects.requireNonNull(createdAt);
  }

  public static PaymentMethod register(UUID id, UUID accountId, PaymentGatewayType type, String tokenRef, Instant now) {
    return new PaymentMethod(id, accountId, type, tokenRef, now);
  }

  public static PaymentMethod reconstitute(UUID id, UUID accountId, PaymentGatewayType type, String tokenRef, Instant createdAt) {
    return new PaymentMethod(id, accountId, type, tokenRef, createdAt);
  }

  private static String requireNonBlank(String value) {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException("tokenRef must not be blank");
    }
    return value;
  }

  public UUID id() {
    return id;
  }

  public UUID accountId() {
    return accountId;
  }

  public PaymentGatewayType type() {
    return type;
  }

  public String tokenRef() {
    return tokenRef;
  }

  public Instant createdAt() {
    return createdAt;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof PaymentMethod other)) return false;
    return id.equals(other.id);
  }

  @Override
  public int hashCode() {
    return id.hashCode();
  }
}
