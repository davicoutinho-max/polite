package dev.civicpulse.membershipaffiliation.adapter.in.web.dto;

import dev.civicpulse.membershipaffiliation.domain.model.Affiliation;
import java.time.Instant;
import java.util.UUID;

public record AffiliationResponse(UUID id, UUID citizenAccountId, UUID partyId, String status, Instant requestedAt, Instant updatedAt) {

  public static AffiliationResponse from(Affiliation affiliation) {
    return new AffiliationResponse(
        affiliation.id(),
        affiliation.citizenAccountId(),
        affiliation.partyId(),
        affiliation.status().code(),
        affiliation.requestedAt().orElse(null),
        affiliation.updatedAt());
  }
}
