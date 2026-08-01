package dev.civicpulse.membershipaffiliation.adapter.out.persistence;

import dev.civicpulse.membershipaffiliation.domain.model.Affiliation;
import org.springframework.stereotype.Component;

@Component
class AffiliationMapper {

  Affiliation toDomain(AffiliationJpaEntity entity) {
    return Affiliation.reconstitute(
        entity.getId(),
        entity.getCitizenAccountId(),
        entity.getPartyId(),
        entity.getStatus(),
        entity.getRequestedAt(),
        entity.getUpdatedAt(),
        entity.getVoterRegistrationNumber(),
        entity.getElectoralZone(),
        entity.getElectoralSection(),
        entity.getElectoralState(),
        entity.getElectoralMunicipality(),
        entity.getIdentityPhotoUrl());
  }

  AffiliationJpaEntity toEntity(Affiliation affiliation) {
    return new AffiliationJpaEntity(
        affiliation.id(),
        affiliation.citizenAccountId(),
        affiliation.partyId(),
        affiliation.status(),
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
