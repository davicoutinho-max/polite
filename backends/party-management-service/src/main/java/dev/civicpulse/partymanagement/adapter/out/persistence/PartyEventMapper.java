package dev.civicpulse.partymanagement.adapter.out.persistence;

import dev.civicpulse.partymanagement.domain.model.PartyEvent;
import org.springframework.stereotype.Component;

@Component
class PartyEventMapper {

  PartyEvent toDomain(PartyEventJpaEntity entity) {
    return PartyEvent.reconstitute(
        entity.getId(),
        entity.getPartyId(),
        entity.getTitle(),
        entity.getEventDate(),
        entity.getLocation(),
        entity.getTagLabel(),
        entity.getTagSeverity());
  }

  PartyEventJpaEntity toEntity(PartyEvent event) {
    return new PartyEventJpaEntity(
        event.id(),
        event.partyId(),
        event.title(),
        event.eventDate(),
        event.location().orElse(null),
        event.tagLabel().orElse(null),
        event.tagSeverity().orElse(null));
  }
}
