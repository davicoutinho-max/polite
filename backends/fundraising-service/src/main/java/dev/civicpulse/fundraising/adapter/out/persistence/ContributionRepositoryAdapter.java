package dev.civicpulse.fundraising.adapter.out.persistence;

import dev.civicpulse.fundraising.application.port.out.ContributionRepository;
import dev.civicpulse.fundraising.domain.model.Contribution;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
class ContributionRepositoryAdapter implements ContributionRepository {

  private final ContributionJpaRepository jpaRepository;
  private final ContributionMapper mapper;

  ContributionRepositoryAdapter(ContributionJpaRepository jpaRepository, ContributionMapper mapper) {
    this.jpaRepository = jpaRepository;
    this.mapper = mapper;
  }

  @Override
  public Contribution save(Contribution contribution) {
    return mapper.toDomain(jpaRepository.save(mapper.toEntity(contribution)));
  }

  @Override
  public List<Contribution> findByFundraiserId(UUID fundraiserId) {
    return jpaRepository.findByFundraiserIdOrderByCreatedAtAsc(fundraiserId).stream().map(mapper::toDomain).toList();
  }

  @Override
  public boolean existsByPaymentIntentId(UUID paymentIntentId) {
    return jpaRepository.existsByPaymentIntentId(paymentIntentId);
  }
}
