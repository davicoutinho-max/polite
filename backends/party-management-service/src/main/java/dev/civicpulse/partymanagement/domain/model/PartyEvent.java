package dev.civicpulse.partymanagement.domain.model;

import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public final class PartyEvent {

  private final UUID id;
  private final UUID partyId;
  private final String title;
  private final LocalDate eventDate;
  private final String location;
  private final String tagLabel;
  private final TagSeverity tagSeverity;

  private PartyEvent(
      UUID id, UUID partyId, String title, LocalDate eventDate, String location, String tagLabel, TagSeverity tagSeverity) {
    this.id = Objects.requireNonNull(id);
    this.partyId = Objects.requireNonNull(partyId);
    this.title = requireNonBlank(title, "title");
    this.eventDate = Objects.requireNonNull(eventDate);
    this.location = location;
    this.tagLabel = tagLabel;
    this.tagSeverity = tagSeverity;
  }

  public static PartyEvent create(
      UUID id, UUID partyId, String title, LocalDate eventDate, String location, String tagLabel, TagSeverity tagSeverity) {
    return new PartyEvent(id, partyId, title, eventDate, location, tagLabel, tagSeverity);
  }

  public static PartyEvent reconstitute(
      UUID id, UUID partyId, String title, LocalDate eventDate, String location, String tagLabel, TagSeverity tagSeverity) {
    return new PartyEvent(id, partyId, title, eventDate, location, tagLabel, tagSeverity);
  }

  private static String requireNonBlank(String value, String field) {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException(field + " must not be blank");
    }
    return value;
  }

  public UUID id() {
    return id;
  }

  public UUID partyId() {
    return partyId;
  }

  public String title() {
    return title;
  }

  public LocalDate eventDate() {
    return eventDate;
  }

  public Optional<String> location() {
    return Optional.ofNullable(location);
  }

  public Optional<String> tagLabel() {
    return Optional.ofNullable(tagLabel);
  }

  public Optional<TagSeverity> tagSeverity() {
    return Optional.ofNullable(tagSeverity);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof PartyEvent other)) return false;
    return id.equals(other.id);
  }

  @Override
  public int hashCode() {
    return id.hashCode();
  }
}
