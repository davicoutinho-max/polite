package dev.civicpulse.legislative.adapter.out.persistence;

import dev.civicpulse.legislative.domain.model.LegislativeItemCategory;
import dev.civicpulse.legislative.domain.model.LegislativeItemStatus;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "legislative_items")
public class LegislativeItemJpaEntity {

  @Id
  @GeneratedValue
  private UUID id;

  @Column(name = "politician_account_id", nullable = false)
  private UUID politicianAccountId;

  @Column(nullable = false)
  private String reference;

  @Column(nullable = false)
  private String title;

  private String summary;

  @Column(nullable = false)
  private LegislativeItemCategory category;

  @Column(nullable = false)
  private LegislativeItemStatus status;

  @Column(name = "item_date", nullable = false)
  private LocalDate itemDate;

  @ElementCollection(fetch = FetchType.EAGER)
  @CollectionTable(name = "legislative_item_cosponsors", joinColumns = @JoinColumn(name = "legislative_item_id"))
  @Column(name = "politician_account_id")
  private Set<UUID> cosponsorAccountIds = new HashSet<>();

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  protected LegislativeItemJpaEntity() {}

  public LegislativeItemJpaEntity(
      UUID id,
      UUID politicianAccountId,
      String reference,
      String title,
      String summary,
      LegislativeItemCategory category,
      LegislativeItemStatus status,
      LocalDate itemDate,
      Set<UUID> cosponsorAccountIds,
      Instant createdAt) {
    this.id = id;
    this.politicianAccountId = politicianAccountId;
    this.reference = reference;
    this.title = title;
    this.summary = summary;
    this.category = category;
    this.status = status;
    this.itemDate = itemDate;
    this.cosponsorAccountIds = new HashSet<>(cosponsorAccountIds);
    this.createdAt = createdAt;
  }

  public UUID getId() {
    return id;
  }

  public UUID getPoliticianAccountId() {
    return politicianAccountId;
  }

  public String getReference() {
    return reference;
  }

  public String getTitle() {
    return title;
  }

  public String getSummary() {
    return summary;
  }

  public LegislativeItemCategory getCategory() {
    return category;
  }

  public LegislativeItemStatus getStatus() {
    return status;
  }

  public LocalDate getItemDate() {
    return itemDate;
  }

  public Set<UUID> getCosponsorAccountIds() {
    return cosponsorAccountIds;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }
}
