package dev.civicpulse.partymanagement.adapter.out.persistence;

import dev.civicpulse.partymanagement.domain.model.PartyRepresentative;
import org.springframework.stereotype.Component;

@Component
class PartyRepresentativeMapper {

  PartyRepresentative toDomain(PartyRepresentativeJpaEntity entity) {
    return PartyRepresentative.reconstitute(
        entity.getId(), entity.getPartyId(), entity.getPoliticianAccountId(), entity.getRoleTitle(), entity.getLinkedAt());
  }

  PartyRepresentativeJpaEntity toEntity(PartyRepresentative representative) {
    return new PartyRepresentativeJpaEntity(
        representative.id(),
        representative.partyId(),
        representative.politicianAccountId(),
        representative.roleTitle().orElse(null),
        representative.linkedAt());
  }
}
