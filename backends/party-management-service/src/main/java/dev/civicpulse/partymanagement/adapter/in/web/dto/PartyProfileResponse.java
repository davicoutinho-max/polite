package dev.civicpulse.partymanagement.adapter.in.web.dto;

import dev.civicpulse.partymanagement.domain.model.PartyProfile;
import java.time.Instant;
import java.util.UUID;

public record PartyProfileResponse(UUID partyId, String history, String program, String statuteUrl, String coverUrl, Instant updatedAt) {

  public static PartyProfileResponse from(PartyProfile profile) {
    return new PartyProfileResponse(
        profile.partyId(),
        profile.history().orElse(null),
        profile.program().orElse(null),
        profile.statuteUrl().orElse(null),
        profile.coverUrl().orElse(null),
        profile.updatedAt());
  }
}
