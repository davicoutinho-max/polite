package dev.civicpulse.legislative.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "transparency_reports")
public class TransparencyReportJpaEntity {

  @Id
  @Column(name = "politician_account_id")
  private UUID politicianAccountId;

  @Column(name = "total_expense_cents", nullable = false)
  private long totalExpenseCents;

  @Column(name = "last_update", nullable = false)
  private LocalDate lastUpdate;

  protected TransparencyReportJpaEntity() {}

  public TransparencyReportJpaEntity(UUID politicianAccountId, long totalExpenseCents, LocalDate lastUpdate) {
    this.politicianAccountId = politicianAccountId;
    this.totalExpenseCents = totalExpenseCents;
    this.lastUpdate = lastUpdate;
  }

  public UUID getPoliticianAccountId() {
    return politicianAccountId;
  }

  public long getTotalExpenseCents() {
    return totalExpenseCents;
  }

  public LocalDate getLastUpdate() {
    return lastUpdate;
  }
}
