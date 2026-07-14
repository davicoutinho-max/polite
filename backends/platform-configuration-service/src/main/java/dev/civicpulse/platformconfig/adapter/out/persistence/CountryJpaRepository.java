package dev.civicpulse.platformconfig.adapter.out.persistence;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface CountryJpaRepository extends JpaRepository<CountryJpaEntity, UUID> {}
