package dev.civicpulse.partymanagement.adapter.out.persistence;

import dev.civicpulse.partymanagement.domain.model.PartyMember;
import org.springframework.stereotype.Component;

@Component
class PartyMemberMapper {

  PartyMember toDomain(PartyMemberJpaEntity entity) {
    return PartyMember.reconstitute(
        entity.getId(), entity.getPartyId(), entity.getCitizenAccountId(), entity.getCity(), entity.getStatus(), entity.getJoinedAt());
  }

  PartyMemberJpaEntity toEntity(PartyMember member) {
    return new PartyMemberJpaEntity(
        member.id(), member.partyId(), member.citizenAccountId(), member.city().orElse(null), member.status(), member.joinedAt());
  }
}
