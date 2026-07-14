package dev.civicpulse.partymanagement.adapter.out.persistence;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface PartyOfficeJpaRepository extends JpaRepository<PartyOfficeJpaEntity, UUID> {

  List<PartyOfficeJpaEntity> findByPartyId(UUID partyId);
}
