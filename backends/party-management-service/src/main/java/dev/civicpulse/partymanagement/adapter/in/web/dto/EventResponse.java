package dev.civicpulse.partymanagement.adapter.in.web.dto;

import dev.civicpulse.partymanagement.domain.model.PartyEvent;
import java.time.LocalDate;
import java.util.UUID;

public record EventResponse(UUID id, UUID partyId, String title, LocalDate eventDate, String location, String tagLabel, String tagSeverity) {

  public static EventResponse from(PartyEvent event) {
    return new EventResponse(
        event.id(),
        event.partyId(),
        event.title(),
        event.eventDate(),
        event.location().orElse(null),
        event.tagLabel().orElse(null),
        event.tagSeverity().map(s -> s.code()).orElse(null));
  }
}
