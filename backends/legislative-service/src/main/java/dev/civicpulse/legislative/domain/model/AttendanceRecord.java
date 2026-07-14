package dev.civicpulse.legislative.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/** {@code presenceRate} is intentionally not stored — always computed from present/absent, fixing
 * a class of bug in the original frontend mock where the stored rate didn't match its own
 * present/absent numbers (see schema.sql's table comment). */
public final class AttendanceRecord {

  private final UUID politicianAccountId;
  private int present;
  private int absent;
  private Instant updatedAt;

  private AttendanceRecord(UUID politicianAccountId, int present, int absent, Instant updatedAt) {
    this.politicianAccountId = Objects.requireNonNull(politicianAccountId);
    this.present = present;
    this.absent = absent;
    this.updatedAt = Objects.requireNonNull(updatedAt);
  }

  public static AttendanceRecord initialize(UUID politicianAccountId, Instant now) {
    return new AttendanceRecord(politicianAccountId, 0, 0, now);
  }

  public static AttendanceRecord reconstitute(UUID politicianAccountId, int present, int absent, Instant updatedAt) {
    return new AttendanceRecord(politicianAccountId, present, absent, updatedAt);
  }

  public void recordPresence(boolean wasPresent, Instant now) {
    if (wasPresent) {
      this.present++;
    } else {
      this.absent++;
    }
    this.updatedAt = now;
  }

  public UUID politicianAccountId() {
    return politicianAccountId;
  }

  public int present() {
    return present;
  }

  public int absent() {
    return absent;
  }

  public double presenceRate() {
    int total = present + absent;
    return total == 0 ? 0.0 : (present * 100.0) / total;
  }

  public Instant updatedAt() {
    return updatedAt;
  }
}
