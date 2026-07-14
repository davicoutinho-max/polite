package dev.civicpulse.partymanagement.adapter.out.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface PartyMemberJpaRepository extends JpaRepository<PartyMemberJpaEntity, UUID> {

  Optional<PartyMemberJpaEntity> findByPartyIdAndCitizenAccountId(UUID partyId, UUID citizenAccountId);

  List<PartyMemberJpaEntity> findByPartyId(UUID partyId);
}
