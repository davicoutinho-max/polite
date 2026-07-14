package dev.civicpulse.legislative.domain.model;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/** {@code share} (% of total) is deliberately not a field here — always computed from
 * {@code amountCents / TransparencyReport.totalExpenseCents} at read time, fixing the original
 * mock's hand-set, internally-inconsistent shares (see schema.sql's table comment). */
public final class ExpenseLine {

  private final UUID id;
  private final UUID politicianAccountId;
  private final String category;
  private final long amountCents;

  private ExpenseLine(UUID id, UUID politicianAccountId, String category, long amountCents) {
    this.id = id;
    this.politicianAccountId = Objects.requireNonNull(politicianAccountId);
    this.category = requireNonBlank(category, "category");
    if (amountCents < 0) {
      throw new IllegalArgumentException("amountCents must not be negative");
    }
    this.amountCents = amountCents;
  }

  public static ExpenseLine record(UUID politicianAccountId, String category, long amountCents) {
    return new ExpenseLine(null, politicianAccountId, category, amountCents);
  }

  public static ExpenseLine reconstitute(UUID id, UUID politicianAccountId, String category, long amountCents) {
    return new ExpenseLine(id, politicianAccountId, category, amountCents);
  }

  private static String requireNonBlank(String value, String field) {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException(field + " must not be blank");
    }
    return value;
  }

  public Optional<UUID> id() {
    return Optional.ofNullable(id);
  }

  public UUID politicianAccountId() {
    return politicianAccountId;
  }

  public String category() {
    return category;
  }

  public long amountCents() {
    return amountCents;
  }

  public double shareOf(long totalExpenseCents) {
    return totalExpenseCents == 0 ? 0.0 : (amountCents * 100.0) / totalExpenseCents;
  }
}
