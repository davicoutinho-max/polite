package dev.civicpulse.platformconfig.adapter.out.persistence;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface PartyRegistryJpaRepository extends JpaRepository<PartyRegistryJpaEntity, UUID> {

  boolean existsByAcronym(String acronym);

  boolean existsByNumber(int number);
}
