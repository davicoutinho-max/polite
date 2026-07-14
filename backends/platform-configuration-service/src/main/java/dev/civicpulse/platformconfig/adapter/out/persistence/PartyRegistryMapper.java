package dev.civicpulse.platformconfig.adapter.out.persistence;

import dev.civicpulse.platformconfig.domain.model.PartyRegistryEntry;
import org.springframework.stereotype.Component;

@Component
class PartyRegistryMapper {

  PartyRegistryEntry toDomain(PartyRegistryJpaEntity entity) {
    return PartyRegistryEntry.reconstitute(
        entity.getId(),
        entity.getName(),
        entity.getAcronym(),
        entity.getNumber(),
        entity.getPresident(),
        entity.getIdeology(),
        entity.getMemberCount(),
        entity.getCreatedAt());
  }

  PartyRegistryJpaEntity toEntity(PartyRegistryEntry entry) {
    return new PartyRegistryJpaEntity(
        entry.id(),
        entry.name(),
        entry.acronym(),
        entry.number(),
        entry.president().orElse(null),
        entry.ideology().orElse(null),
        entry.memberCount(),
        entry.createdAt());
  }
}
