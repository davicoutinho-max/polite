package dev.civicpulse.partymanagement.adapter.out.persistence;

import dev.civicpulse.partymanagement.domain.model.PartyProfile;
import org.springframework.stereotype.Component;

@Component
class PartyProfileMapper {

  PartyProfile toDomain(PartyProfileJpaEntity entity) {
    return PartyProfile.reconstitute(
        entity.getPartyId(), entity.getHistory(), entity.getProgram(), entity.getStatuteUrl(), entity.getCoverUrl(), entity.getUpdatedAt());
  }

  PartyProfileJpaEntity toEntity(PartyProfile profile) {
    return new PartyProfileJpaEntity(
        profile.partyId(),
        profile.history().orElse(null),
        profile.program().orElse(null),
        profile.statuteUrl().orElse(null),
        profile.coverUrl().orElse(null),
        profile.updatedAt());
  }
}
