package dev.civicpulse.partymanagement.adapter.out.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface PartyRepresentativeJpaRepository extends JpaRepository<PartyRepresentativeJpaEntity, UUID> {

  Optional<PartyRepresentativeJpaEntity> findByPartyIdAndPoliticianAccountId(UUID partyId, UUID politicianAccountId);

  boolean existsByPartyIdAndPoliticianAccountId(UUID partyId, UUID politicianAccountId);

  List<PartyRepresentativeJpaEntity> findByPartyId(UUID partyId);
}
