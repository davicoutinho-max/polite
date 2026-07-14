package dev.civicpulse.legislative.adapter.out.persistence;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface PoliticianDossierExtensionJpaRepository extends JpaRepository<PoliticianDossierExtensionJpaEntity, UUID> {}
