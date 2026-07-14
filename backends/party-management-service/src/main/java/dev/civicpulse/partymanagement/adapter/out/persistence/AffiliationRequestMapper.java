package dev.civicpulse.partymanagement.adapter.out.persistence;

import dev.civicpulse.partymanagement.domain.model.AffiliationRequest;
import org.springframework.stereotype.Component;

@Component
class AffiliationRequestMapper {

  AffiliationRequest toDomain(AffiliationRequestJpaEntity entity) {
    return AffiliationRequest.reconstitute(
        entity.getId(),
        entity.getPartyId(),
        entity.getCitizenAccountId(),
        entity.getCity(),
        entity.getStatus(),
        entity.getRequestedAt(),
        entity.getDecidedAt());
  }

  AffiliationRequestJpaEntity toEntity(AffiliationRequest request) {
    return new AffiliationRequestJpaEntity(
        request.id(),
        request.partyId(),
        request.citizenAccountId(),
        request.city().orElse(null),
        request.status(),
        request.requestedAt(),
        request.decidedAt().orElse(null));
  }
}
