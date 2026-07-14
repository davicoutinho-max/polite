package dev.civicpulse.membershipaffiliation.adapter.out.persistence;

import dev.civicpulse.membershipaffiliation.domain.model.Affiliation;
import org.springframework.stereotype.Component;

@Component
class AffiliationMapper {

  Affiliation toDomain(AffiliationJpaEntity entity) {
    return Affiliation.reconstitute(
        entity.getId(), entity.getCitizenAccountId(), entity.getPartyId(), entity.getStatus(), entity.getRequestedAt(), entity.getUpdatedAt());
  }

  AffiliationJpaEntity toEntity(Affiliation affiliation) {
    return new AffiliationJpaEntity(
        affiliation.id(),
        affiliation.citizenAccountId(),
        affiliation.partyId(),
        affiliation.status(),
        affiliation.requestedAt().orElse(null),
        affiliation.updatedAt());
  }
}
