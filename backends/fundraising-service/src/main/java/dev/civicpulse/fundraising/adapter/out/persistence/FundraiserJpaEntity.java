package dev.civicpulse.fundraising.adapter.out.persistence;

import dev.civicpulse.fundraising.domain.model.FundraiserCategory;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "fundraisers")
public class FundraiserJpaEntity {

  @Id private UUID id;

  @Column(name = "organizer_account_id", nullable = false)
  private UUID organizerAccountId;

  @Column(nullable = false)
  private String title;

  private String description;

  @Column(nullable = false)
  private FundraiserCategory category;

  @Column(name = "goal_cents", nullable = false)
  private long goalCents;

  @Column(name = "raised_cents", nullable = false)
  private long raisedCents;

  @Column(name = "supporters_count", nullable = false)
  private int supportersCount;

  private LocalDate deadline;

  @Column(name = "ledger_public", nullable = false)
  private boolean ledgerPublic;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  protected FundraiserJpaEntity() {}

  public FundraiserJpaEntity(
      UUID id,
      UUID organizerAccountId,
      String title,
      String description,
      FundraiserCategory category,
      long goalCents,
      long raisedCents,
      int supportersCount,
      LocalDate deadline,
      boolean ledgerPublic,
      Instant createdAt) {
    this.id = id;
    this.organizerAccountId = organizerAccountId;
    this.title = title;
    this.description = description;
    this.category = category;
    this.goalCents = goalCents;
    this.raisedCents = raisedCents;
    this.supportersCount = supportersCount;
    this.deadline = deadline;
    this.ledgerPublic = ledgerPublic;
    this.createdAt = createdAt;
  }

  public UUID getId() {
    return id;
  }

  public UUID getOrganizerAccountId() {
    return organizerAccountId;
  }

  public String getTitle() {
    return title;
  }

  public String getDescription() {
    return description;
  }

  public FundraiserCategory getCategory() {
    return category;
  }

  public long getGoalCents() {
    return goalCents;
  }

  public long getRaisedCents() {
    return raisedCents;
  }

  public int getSupportersCount() {
    return supportersCount;
  }

  public LocalDate getDeadline() {
    return deadline;
  }

  public boolean isLedgerPublic() {
    return ledgerPublic;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }
}
