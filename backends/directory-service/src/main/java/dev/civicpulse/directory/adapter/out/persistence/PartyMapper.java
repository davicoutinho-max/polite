package dev.civicpulse.directory.adapter.out.persistence;

import dev.civicpulse.directory.domain.model.Party;
import org.springframework.stereotype.Component;

@Component
class PartyMapper {

  Party toDomain(PartyJpaEntity entity) {
    return Party.reconstitute(
        entity.getId(),
        entity.getName(),
        entity.getAcronym(),
        entity.getNumber(),
        entity.getIdeology(),
        entity.getSpectrum(),
        entity.getFoundedYear(),
        entity.getPresident(),
        entity.getLogoUrl(),
        entity.getMemberCount(),
        entity.getUpdatedAt());
  }

  PartyJpaEntity toEntity(Party party) {
    return new PartyJpaEntity(
        party.id(),
        party.name(),
        party.acronym(),
        party.number(),
        party.ideology().orElse(null),
        party.spectrum().orElse(null),
        party.foundedYear().orElse(null),
        party.president().orElse(null),
        party.logoUrl().orElse(null),
        party.memberCount(),
        party.updatedAt());
  }
}
