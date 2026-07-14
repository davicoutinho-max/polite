package dev.civicpulse.partymanagement.adapter.out.persistence;

import dev.civicpulse.partymanagement.domain.model.PartyOffice;
import org.springframework.stereotype.Component;

@Component
class PartyOfficeMapper {

  PartyOffice toDomain(PartyOfficeJpaEntity entity) {
    return PartyOffice.reconstitute(
        entity.getId(), entity.getPartyId(), entity.getScope(), entity.getLocation(), entity.getLeaderName(), entity.getMemberCount());
  }

  PartyOfficeJpaEntity toEntity(PartyOffice office) {
    return new PartyOfficeJpaEntity(
        office.id(), office.partyId(), office.scope(), office.location(), office.leaderName().orElse(null), office.memberCount());
  }
}
