package dev.civicpulse.legislative.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "attendance_records")
public class AttendanceRecordJpaEntity {

  @Id
  @Column(name = "politician_account_id")
  private UUID politicianAccountId;

  @Column(nullable = false)
  private int present;

  @Column(nullable = false)
  private int absent;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  protected AttendanceRecordJpaEntity() {}

  public AttendanceRecordJpaEntity(UUID politicianAccountId, int present, int absent, Instant updatedAt) {
    this.politicianAccountId = politicianAccountId;
    this.present = present;
    this.absent = absent;
    this.updatedAt = updatedAt;
  }

  public UUID getPoliticianAccountId() {
    return politicianAccountId;
  }

  public int getPresent() {
    return present;
  }

  public int getAbsent() {
    return absent;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }
}
