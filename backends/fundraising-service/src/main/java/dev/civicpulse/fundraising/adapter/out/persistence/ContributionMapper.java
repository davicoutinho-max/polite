package dev.civicpulse.fundraising.adapter.out.persistence;

import dev.civicpulse.fundraising.domain.model.Contribution;
import org.springframework.stereotype.Component;

@Component
class ContributionMapper {

  Contribution toDomain(ContributionJpaEntity entity) {
    return Contribution.reconstitute(
        entity.getId(), entity.getFundraiserId(), entity.getSupporterAccountId(), entity.getAmountCents(), entity.getPaymentIntentId(),
        entity.getCreatedAt());
  }

  ContributionJpaEntity toEntity(Contribution contribution) {
    return new ContributionJpaEntity(
        contribution.id(),
        contribution.fundraiserId(),
        contribution.supporterAccountId(),
        contribution.amountCents(),
        contribution.paymentIntentId(),
        contribution.createdAt());
  }
}
