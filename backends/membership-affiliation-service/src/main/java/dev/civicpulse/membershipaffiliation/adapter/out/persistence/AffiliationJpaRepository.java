package dev.civicpulse.membershipaffiliation.adapter.out.persistence;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface AffiliationJpaRepository extends JpaRepository<AffiliationJpaEntity, UUID> {

  @Query(
      "select case when count(a) > 0 then true else false end from AffiliationJpaEntity a "
          + "where a.citizenAccountId = :citizenAccountId and a.partyId = :partyId "
          + "and a.status <> dev.civicpulse.membershipaffiliation.domain.model.AffiliationStatus.REJECTED")
  boolean existsActiveByCitizenAndParty(@Param("citizenAccountId") UUID citizenAccountId, @Param("partyId") UUID partyId);

  List<AffiliationJpaEntity> findByCitizenAccountId(UUID citizenAccountId);
}
