package dev.civicpulse.legislative.adapter.out.persistence;

import dev.civicpulse.legislative.domain.model.VoteChoice;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "vote_records")
public class VoteRecordJpaEntity {

  @Id
  @GeneratedValue
  private UUID id;

  @Column(name = "politician_account_id", nullable = false)
  private UUID politicianAccountId;

  @Column(name = "legislative_item_id")
  private UUID legislativeItemId;

  @Column(nullable = false)
  private String matter;

  @Column(name = "vote_date", nullable = false)
  private LocalDate voteDate;

  @Column(nullable = false)
  private VoteChoice choice;

  protected VoteRecordJpaEntity() {}

  public VoteRecordJpaEntity(UUID id, UUID politicianAccountId, UUID legislativeItemId, String matter, LocalDate voteDate, VoteChoice choice) {
    this.id = id;
    this.politicianAccountId = politicianAccountId;
    this.legislativeItemId = legislativeItemId;
    this.matter = matter;
    this.voteDate = voteDate;
    this.choice = choice;
  }

  public UUID getId() {
    return id;
  }

  public UUID getPoliticianAccountId() {
    return politicianAccountId;
  }

  public UUID getLegislativeItemId() {
    return legislativeItemId;
  }

  public String getMatter() {
    return matter;
  }

  public LocalDate getVoteDate() {
    return voteDate;
  }

  public VoteChoice getChoice() {
    return choice;
  }
}
