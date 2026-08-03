package dev.civicpulse.identity.adapter.out.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface RegistrationTokenJpaRepository extends JpaRepository<RegistrationTokenJpaEntity, UUID> {

  Optional<RegistrationTokenJpaEntity> findByToken(String token);

  List<RegistrationTokenJpaEntity> findByIssuedByAccountIdOrderByCreatedAtDesc(UUID issuedByAccountId);
}
