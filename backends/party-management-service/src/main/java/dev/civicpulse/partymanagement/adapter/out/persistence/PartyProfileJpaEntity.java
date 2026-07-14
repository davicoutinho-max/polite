package dev.civicpulse.partymanagement.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "party_profiles")
public class PartyProfileJpaEntity {

  @Id
  @Column(name = "party_id")
  private UUID partyId;

  private String history;
  private String program;

  @Column(name = "statute_url")
  private String statuteUrl;

  @Column(name = "cover_url")
  private String coverUrl;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  protected PartyProfileJpaEntity() {}

  public PartyProfileJpaEntity(UUID partyId, String history, String program, String statuteUrl, String coverUrl, Instant updatedAt) {
    this.partyId = partyId;
    this.history = history;
    this.program = program;
    this.statuteUrl = statuteUrl;
    this.coverUrl = coverUrl;
    this.updatedAt = updatedAt;
  }

  public UUID getPartyId() {
    return partyId;
  }

  public String getHistory() {
    return history;
  }

  public String getProgram() {
    return program;
  }

  public String getStatuteUrl() {
    return statuteUrl;
  }

  public String getCoverUrl() {
    return coverUrl;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }
}
