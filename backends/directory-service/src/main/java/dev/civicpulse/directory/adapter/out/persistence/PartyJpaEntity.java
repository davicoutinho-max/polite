package dev.civicpulse.directory.adapter.out.persistence;

import dev.civicpulse.directory.domain.model.PartySpectrum;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "parties")
public class PartyJpaEntity {

  @Id private UUID id;

  @Column(nullable = false)
  private String name;

  @Column(nullable = false, unique = true)
  private String acronym;

  @Column(nullable = false)
  private int number;

  private String ideology;

  private PartySpectrum spectrum;

  @Column(name = "founded_year")
  private Integer foundedYear;

  private String president;

  @Column(name = "logo_url")
  private String logoUrl;

  @Column(name = "member_count", nullable = false)
  private int memberCount;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  protected PartyJpaEntity() {}

  public PartyJpaEntity(
      UUID id,
      String name,
      String acronym,
      int number,
      String ideology,
      PartySpectrum spectrum,
      Integer foundedYear,
      String president,
      String logoUrl,
      int memberCount,
      Instant updatedAt) {
    this.id = id;
    this.name = name;
    this.acronym = acronym;
    this.number = number;
    this.ideology = ideology;
    this.spectrum = spectrum;
    this.foundedYear = foundedYear;
    this.president = president;
    this.logoUrl = logoUrl;
    this.memberCount = memberCount;
    this.updatedAt = updatedAt;
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

  public String getIdeology() {
    return ideology;
  }

  public PartySpectrum getSpectrum() {
    return spectrum;
  }

  public Integer getFoundedYear() {
    return foundedYear;
  }

  public String getPresident() {
    return president;
  }

  public String getLogoUrl() {
    return logoUrl;
  }

  public int getMemberCount() {
    return memberCount;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }
}
