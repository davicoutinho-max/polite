package dev.civicpulse.directory.adapter.out.persistence;

import dev.civicpulse.directory.domain.model.Politician;
import org.springframework.stereotype.Component;

@Component
class PoliticianMapper {

  Politician toDomain(PoliticianJpaEntity entity) {
    return Politician.reconstitute(
        entity.getAccountId(),
        entity.getName(),
        entity.getHandle(),
        entity.getAvatarUrl(),
        entity.isVerified(),
        entity.getOffice(),
        entity.getLevel(),
        entity.getPartyId(),
        entity.getPartyAcronym(),
        entity.getState(),
        entity.getFollowersCount(),
        entity.getBillsCount(),
        entity.getUpdatedAt());
  }

  PoliticianJpaEntity toEntity(Politician politician) {
    return new PoliticianJpaEntity(
        politician.accountId(),
        politician.name(),
        politician.handle(),
        politician.avatarUrl().orElse(null),
        politician.verified(),
        politician.office().orElse(null),
        politician.level().orElse(null),
        politician.partyId().orElse(null),
        politician.partyAcronym().orElse(null),
        politician.state().orElse(null),
        politician.followersCount(),
        politician.billsCount(),
        politician.updatedAt());
  }
}
