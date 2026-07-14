package dev.civicpulse.privacycompliance.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

/** {@code purpose} is a plain {@code String}, not the {@code ConsentPurpose} enum — {@code
 * @Converter}(autoApply) is silently ignored on {@code @Id} fields per the JPA spec (see
 * directory-service's {@code FollowJpaEntity.targetType} for the identical constraint), so
 * conversion happens manually in {@code ConsentRecordMapper} instead. */
@Entity
@Table(name = "consent_records")
@IdClass(ConsentRecordId.class)
public class ConsentRecordJpaEntity {

  @Id
  @Column(name = "account_id")
  private UUID accountId;

  @Id
  @Column(name = "purpose")
  private String purpose;

  @Column(nullable = false)
  private boolean granted;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  protected ConsentRecordJpaEntity() {}

  public ConsentRecordJpaEntity(UUID accountId, String purpose, boolean granted, Instant updatedAt) {
    this.accountId = accountId;
    this.purpose = purpose;
    this.granted = granted;
    this.updatedAt = updatedAt;
  }

  public UUID getAccountId() {
    return accountId;
  }

  public String getPurpose() {
    return purpose;
  }

  public boolean isGranted() {
    return granted;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }
}
