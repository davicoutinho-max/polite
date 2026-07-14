package dev.civicpulse.participation.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "petitions")
public class PetitionJpaEntity {

  @Id private UUID id;

  @Column(nullable = false)
  private String title;

  private String summary;

  private String category;

  @Column(nullable = false)
  private int goal;

  @Column(name = "signatures_count", nullable = false)
  private int signaturesCount;

  private LocalDate deadline;

  protected PetitionJpaEntity() {}

  public PetitionJpaEntity(UUID id, String title, String summary, String category, int goal, int signaturesCount, LocalDate deadline) {
    this.id = id;
    this.title = title;
    this.summary = summary;
    this.category = category;
    this.goal = goal;
    this.signaturesCount = signaturesCount;
    this.deadline = deadline;
  }

  public UUID getId() {
    return id;
  }

  public String getTitle() {
    return title;
  }

  public String getSummary() {
    return summary;
  }

  public String getCategory() {
    return category;
  }

  public int getGoal() {
    return goal;
  }

  public int getSignaturesCount() {
    return signaturesCount;
  }

  public LocalDate getDeadline() {
    return deadline;
  }
}
