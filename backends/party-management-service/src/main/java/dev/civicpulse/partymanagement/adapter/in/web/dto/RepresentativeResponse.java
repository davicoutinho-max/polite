package dev.civicpulse.partymanagement.adapter.in.web.dto;

import dev.civicpulse.partymanagement.domain.model.PartyRepresentative;
import java.time.Instant;
import java.util.UUID;

public record RepresentativeResponse(UUID id, UUID partyId, UUID politicianAccountId, String roleTitle, Instant linkedAt) {

  public static RepresentativeResponse from(PartyRepresentative representative) {
    return new RepresentativeResponse(
        representative.id(),
        representative.partyId(),
        representative.politicianAccountId(),
        representative.roleTitle().orElse(null),
        representative.linkedAt());
  }
}
