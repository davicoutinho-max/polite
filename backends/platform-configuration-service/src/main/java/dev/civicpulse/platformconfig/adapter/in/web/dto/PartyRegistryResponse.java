package dev.civicpulse.platformconfig.adapter.in.web.dto;

import dev.civicpulse.platformconfig.domain.model.PartyRegistryEntry;
import java.time.Instant;
import java.util.UUID;

public record PartyRegistryResponse(
    UUID id, String name, String acronym, int number, String president, String ideology, int memberCount, Instant createdAt) {

  public static PartyRegistryResponse from(PartyRegistryEntry entry) {
    return new PartyRegistryResponse(
        entry.id(),
        entry.name(),
        entry.acronym(),
        entry.number(),
        entry.president().orElse(null),
        entry.ideology().orElse(null),
        entry.memberCount(),
        entry.createdAt());
  }
}
