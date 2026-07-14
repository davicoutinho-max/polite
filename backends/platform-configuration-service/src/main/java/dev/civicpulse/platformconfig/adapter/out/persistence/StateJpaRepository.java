package dev.civicpulse.platformconfig.adapter.out.persistence;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface StateJpaRepository extends JpaRepository<StateJpaEntity, UUID> {

  List<StateJpaEntity> findByCountryId(UUID countryId);
}
