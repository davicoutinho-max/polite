package dev.civicpulse.membershipaffiliation.adapter.out.persistence;

import dev.civicpulse.membershipaffiliation.application.port.out.MembershipFeeRepository;
import dev.civicpulse.membershipaffiliation.domain.model.FeeStatus;
import dev.civicpulse.membershipaffiliation.domain.model.MembershipFee;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
class MembershipFeeRepositoryAdapter implements MembershipFeeRepository {

  private final MembershipFeeJpaRepository jpaRepository;

  MembershipFeeRepositoryAdapter(MembershipFeeJpaRepository jpaRepository) {
    this.jpaRepository = jpaRepository;
  }

  @Override
  public MembershipFee save(MembershipFee fee) {
    var saved =
        jpaRepository.save(
            new MembershipFeeJpaEntity(
                fee.id(),
                fee.affiliationId(),
                fee.referencePeriod(),
                fee.amountCents(),
                fee.dueDate(),
                fee.status(),
                fee.paidAt().orElse(null),
                fee.paymentIntentId().orElse(null)));
    return toDomain(saved);
  }

  @Override
  public Optional<MembershipFee> findById(UUID id) {
    return jpaRepository.findById(id).map(MembershipFeeRepositoryAdapter::toDomain);
  }

  @Override
  public List<MembershipFee> findByAffiliationId(UUID affiliationId) {
    return jpaRepository.findByAffiliationId(affiliationId).stream().map(MembershipFeeRepositoryAdapter::toDomain).toList();
  }

  @Override
  public List<MembershipFee> findByStatusAndDueDateBefore(FeeStatus status, LocalDate date) {
    return jpaRepository.findByStatusAndDueDateBefore(status, date).stream().map(MembershipFeeRepositoryAdapter::toDomain).toList();
  }

  private static MembershipFee toDomain(MembershipFeeJpaEntity entity) {
    return MembershipFee.reconstitute(
        entity.getId(),
        entity.getAffiliationId(),
        entity.getReferencePeriod(),
        entity.getAmountCents(),
        entity.getDueDate(),
        entity.getStatus(),
        entity.getPaidAt(),
        entity.getPaymentIntentId());
  }
}
