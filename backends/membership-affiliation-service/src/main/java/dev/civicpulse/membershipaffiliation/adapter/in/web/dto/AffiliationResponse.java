package dev.civicpulse.membershipaffiliation.adapter.in.web.dto;

import dev.civicpulse.membershipaffiliation.domain.model.Affiliation;
import java.time.Instant;
import java.util.UUID;

public record AffiliationResponse(
    UUID id,
    UUID citizenAccountId,
    UUID partyId,
    String status,
    Instant requestedAt,
    Instant updatedAt,
    String voterRegistrationNumber,
    String electoralZone,
    String electoralSection,
    String electoralState,
    String electoralMunicipality,
    String identityPhotoUrl) {

  public static AffiliationResponse from(Affiliation affiliation) {
    return new AffiliationResponse(
        affiliation.id(),
        affiliation.citizenAccountId(),
        affiliation.partyId(),
        affiliation.status().code(),
        affiliation.requestedAt().orElse(null),
        affiliation.updatedAt(),
        affiliation.voterRegistrationNumber().orElse(null),
        affiliation.electoralZone().orElse(null),
        affiliation.electoralSection().orElse(null),
        affiliation.electoralState().orElse(null),
        affiliation.electoralMunicipality().orElse(null),
        affiliation.identityPhotoUrl().orElse(null));
  }
}
