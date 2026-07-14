package dev.civicpulse.fundraising.adapter.out.persistence;

import dev.civicpulse.fundraising.domain.model.Fundraiser;
import org.springframework.stereotype.Component;

@Component
class FundraiserMapper {

  Fundraiser toDomain(FundraiserJpaEntity entity) {
    return Fundraiser.reconstitute(
        entity.getId(),
        entity.getOrganizerAccountId(),
        entity.getTitle(),
        entity.getDescription(),
        entity.getCategory(),
        entity.getGoalCents(),
        entity.getRaisedCents(),
        entity.getSupportersCount(),
        entity.getDeadline(),
        entity.isLedgerPublic(),
        entity.getCreatedAt());
  }

  FundraiserJpaEntity toEntity(Fundraiser fundraiser) {
    return new FundraiserJpaEntity(
        fundraiser.id(),
        fundraiser.organizerAccountId(),
        fundraiser.title(),
        fundraiser.description().orElse(null),
        fundraiser.category(),
        fundraiser.goalCents(),
        fundraiser.raisedCents(),
        fundraiser.supportersCount(),
        fundraiser.deadline().orElse(null),
        fundraiser.ledgerPublic(),
        fundraiser.createdAt());
  }
}
