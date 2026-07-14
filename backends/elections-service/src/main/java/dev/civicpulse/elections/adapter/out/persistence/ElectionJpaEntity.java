package dev.civicpulse.elections.adapter.out.persistence;

import dev.civicpulse.elections.domain.model.ElectionScope;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "elections")
public class ElectionJpaEntity {

  @Id private UUID id;

  @Column(nullable = false)
  private String title;

  @Column(nullable = false)
  private ElectionScope scope;

  @Column(name = "election_date", nullable = false)
  private LocalDate electionDate;

  private String description;

  protected ElectionJpaEntity() {}

  public ElectionJpaEntity(UUID id, String title, ElectionScope scope, LocalDate electionDate, String description) {
    this.id = id;
    this.title = title;
    this.scope = scope;
    this.electionDate = electionDate;
    this.description = description;
  }

  public UUID getId() {
    return id;
  }

  public String getTitle() {
    return title;
  }

  public ElectionScope getScope() {
    return scope;
  }

  public LocalDate getElectionDate() {
    return electionDate;
  }

  public String getDescription() {
    return description;
  }
}
