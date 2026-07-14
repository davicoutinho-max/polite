package dev.civicpulse.platformconfig.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "party_registry")
public class PartyRegistryJpaEntity {

  @Id private UUID id;

  @Column(nullable = false)
  private String name;

  @Column(nullable = false, unique = true)
  private String acronym;

  @Column(nullable = false, unique = true)
  private int number;

  private String president;
  private String ideology;

  @Column(name = "member_count", nullable = false)
  private int memberCount;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  protected PartyRegistryJpaEntity() {}

  public PartyRegistryJpaEntity(
      UUID id, String name, String acronym, int number, String president, String ideology, int memberCount, Instant createdAt) {
    this.id = id;
    this.name = name;
    this.acronym = acronym;
    this.number = number;
    this.president = president;
    this.ideology = ideology;
    this.memberCount = memberCount;
    this.createdAt = createdAt;
  }

  public UUID getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public String getAcronym() {
    return acronym;
  }

  public int getNumber() {
    return number;
  }

  public String getPresident() {
    return president;
  }

  public String getIdeology() {
    return ideology;
  }

  public int getMemberCount() {
    return memberCount;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }
}
