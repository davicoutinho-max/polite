package dev.civicpulse.legislative.adapter.out.persistence;

import dev.civicpulse.legislative.domain.model.LegislativeItem;
import org.springframework.stereotype.Component;

@Component
class LegislativeItemMapper {

  LegislativeItem toDomain(LegislativeItemJpaEntity entity) {
    return LegislativeItem.reconstitute(
        entity.getId(),
        entity.getPoliticianAccountId(),
        entity.getReference(),
        entity.getTitle(),
        entity.getSummary(),
        entity.getCategory(),
        entity.getStatus(),
        entity.getItemDate(),
        entity.getCosponsorAccountIds(),
        entity.getCreatedAt());
  }

  LegislativeItemJpaEntity toEntity(LegislativeItem item) {
    return new LegislativeItemJpaEntity(
        item.id().orElse(null),
        item.politicianAccountId(),
        item.reference(),
        item.title(),
        item.summary().orElse(null),
        item.category(),
        item.status(),
        item.itemDate(),
        item.cosponsorAccountIds(),
        item.createdAt());
  }
}
