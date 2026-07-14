package dev.civicpulse.membershipaffiliation.adapter.out.persistence;

import dev.civicpulse.membershipaffiliation.application.port.out.MembershipCardRepository;
import dev.civicpulse.membershipaffiliation.domain.model.MembershipCard;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
class MembershipCardRepositoryAdapter implements MembershipCardRepository {

  private final MembershipCardJpaRepository jpaRepository;

  MembershipCardRepositoryAdapter(MembershipCardJpaRepository jpaRepository) {
    this.jpaRepository = jpaRepository;
  }

  @Override
  public MembershipCard save(MembershipCard card) {
    var saved =
        jpaRepository.save(new MembershipCardJpaEntity(card.affiliationId(), card.memberNumber(), card.qrPayload(), card.issuedAt()));
    return toDomain(saved);
  }

  @Override
  public Optional<MembershipCard> findByAffiliationId(UUID affiliationId) {
    return jpaRepository.findById(affiliationId).map(MembershipCardRepositoryAdapter::toDomain);
  }

  @Override
  public boolean existsByMemberNumber(String memberNumber) {
    return jpaRepository.existsByMemberNumber(memberNumber);
  }

  private static MembershipCard toDomain(MembershipCardJpaEntity entity) {
    return MembershipCard.reconstitute(entity.getAffiliationId(), entity.getMemberNumber(), entity.getQrPayload(), entity.getIssuedAt());
  }
}
