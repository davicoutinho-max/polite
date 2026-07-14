package dev.civicpulse.partymanagement.adapter.out.persistence;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface PartyEventJpaRepository extends JpaRepository<PartyEventJpaEntity, UUID> {

  List<PartyEventJpaEntity> findByPartyIdOrderByEventDateAsc(UUID partyId);
}
