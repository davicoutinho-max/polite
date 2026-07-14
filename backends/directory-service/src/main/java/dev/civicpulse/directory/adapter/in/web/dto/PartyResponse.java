package dev.civicpulse.directory.adapter.in.web.dto;

import dev.civicpulse.directory.domain.model.Party;
import java.time.Instant;
import java.util.UUID;

public record PartyResponse(
    UUID id,
    String name,
    String acronym,
    int number,
    String ideology,
    String spectrum,
    Integer foundedYear,
    String president,
    String logoUrl,
    int memberCount,
    Instant updatedAt) {

  public static PartyResponse from(Party party) {
    return new PartyResponse(
        party.id(),
        party.name(),
        party.acronym(),
        party.number(),
        party.ideology().orElse(null),
        party.spectrum().map(s -> s.code()).orElse(null),
        party.foundedYear().orElse(null),
        party.president().orElse(null),
        party.logoUrl().orElse(null),
        party.memberCount(),
        party.updatedAt());
  }
}
