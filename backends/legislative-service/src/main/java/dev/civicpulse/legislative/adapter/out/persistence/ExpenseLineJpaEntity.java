package dev.civicpulse.legislative.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "expense_lines")
public class ExpenseLineJpaEntity {

  @Id
  @GeneratedValue
  private UUID id;

  @Column(name = "politician_account_id", nullable = false)
  private UUID politicianAccountId;

  @Column(nullable = false)
  private String category;

  @Column(name = "amount_cents", nullable = false)
  private long amountCents;

  protected ExpenseLineJpaEntity() {}

  public ExpenseLineJpaEntity(UUID id, UUID politicianAccountId, String category, long amountCents) {
    this.id = id;
    this.politicianAccountId = politicianAccountId;
    this.category = category;
    this.amountCents = amountCents;
  }

  public UUID getId() {
    return id;
  }

  public UUID getPoliticianAccountId() {
    return politicianAccountId;
  }

  public String getCategory() {
    return category;
  }

  public long getAmountCents() {
    return amountCents;
  }
}
