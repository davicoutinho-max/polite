package dev.civicpulse.legislative.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "mandates")
public class MandateJpaEntity {

  @Id
  @GeneratedValue
  private UUID id;

  @Column(name = "politician_account_id", nullable = false)
  private UUID politicianAccountId;

  @Column(nullable = false)
  private String role;

  @Column(nullable = false)
  private String period;

  @Column(nullable = false)
  private boolean current;

  protected MandateJpaEntity() {}

  public MandateJpaEntity(UUID id, UUID politicianAccountId, String role, String period, boolean current) {
    this.id = id;
    this.politicianAccountId = politicianAccountId;
    this.role = role;
    this.period = period;
    this.current = current;
  }

  public UUID getId() {
    return id;
  }

  public UUID getPoliticianAccountId() {
    return politicianAccountId;
  }

  public String getRole() {
    return role;
  }

  public String getPeriod() {
    return period;
  }

  public boolean isCurrent() {
    return current;
  }
}
