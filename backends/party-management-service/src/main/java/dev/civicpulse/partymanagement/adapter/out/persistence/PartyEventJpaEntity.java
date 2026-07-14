package dev.civicpulse.partymanagement.adapter.out.persistence;

import dev.civicpulse.partymanagement.domain.model.TagSeverity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "party_events")
public class PartyEventJpaEntity {

  @Id private UUID id;

  @Column(name = "party_id", nullable = false)
  private UUID partyId;

  @Column(nullable = false)
  private String title;

  @Column(name = "event_date", nullable = false)
  private LocalDate eventDate;

  private String location;

  @Column(name = "tag_label")
  private String tagLabel;

  @Column(name = "tag_severity")
  private TagSeverity tagSeverity;

  protected PartyEventJpaEntity() {}

  public PartyEventJpaEntity(
      UUID id, UUID partyId, String title, LocalDate eventDate, String location, String tagLabel, TagSeverity tagSeverity) {
    this.id = id;
    this.partyId = partyId;
    this.title = title;
    this.eventDate = eventDate;
    this.location = location;
    this.tagLabel = tagLabel;
    this.tagSeverity = tagSeverity;
  }

  public UUID getId() {
    return id;
  }

  public UUID getPartyId() {
    return partyId;
  }

  public String getTitle() {
    return title;
  }

  public LocalDate getEventDate() {
    return eventDate;
  }

  public String getLocation() {
    return location;
  }

  public String getTagLabel() {
    return tagLabel;
  }

  public TagSeverity getTagSeverity() {
    return tagSeverity;
  }
}
