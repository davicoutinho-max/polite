package dev.civicpulse.legislative.domain.model;

import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

/** {@code totalExpenseCents} is always recomputed at write time from the sum of the politician's
 * {@link ExpenseLine}s, never trusted as an independent input — same reasoning as
 * {@link AttendanceRecord}'s computed presence rate (see schema.sql's table comment). */
public final class TransparencyReport {

  private final UUID politicianAccountId;
  private long totalExpenseCents;
  private LocalDate lastUpdate;

  private TransparencyReport(UUID politicianAccountId, long totalExpenseCents, LocalDate lastUpdate) {
    this.politicianAccountId = Objects.requireNonNull(politicianAccountId);
    this.totalExpenseCents = totalExpenseCents;
    this.lastUpdate = Objects.requireNonNull(lastUpdate);
  }

  public static TransparencyReport initialize(UUID politicianAccountId, LocalDate today) {
    return new TransparencyReport(politicianAccountId, 0L, today);
  }

  public static TransparencyReport reconstitute(UUID politicianAccountId, long totalExpenseCents, LocalDate lastUpdate) {
    return new TransparencyReport(politicianAccountId, totalExpenseCents, lastUpdate);
  }

  public void recomputeTotal(long totalExpenseCents, LocalDate today) {
    this.totalExpenseCents = totalExpenseCents;
    this.lastUpdate = today;
  }

  public UUID politicianAccountId() {
    return politicianAccountId;
  }

  public long totalExpenseCents() {
    return totalExpenseCents;
  }

  public LocalDate lastUpdate() {
    return lastUpdate;
  }
}
