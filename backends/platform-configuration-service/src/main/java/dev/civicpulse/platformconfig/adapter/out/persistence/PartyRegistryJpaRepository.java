package dev.civicpulse.platformconfig.adapter.out.persistence;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface PartyRegistryJpaRepository extends JpaRepository<PartyRegistryJpaEntity, UUID> {

  Optional<PartyRegistryJpaEntity> findByAcronym(String acronym);

  boolean existsByAcronym(String acronym);

  boolean existsByNumber(int number);
}
