package dev.civicpulse.identity.adapter.out.persistence;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface AccountJpaRepository extends JpaRepository<AccountJpaEntity, UUID> {

  Optional<AccountJpaEntity> findByEmail(String email);

  Optional<AccountJpaEntity> findByHandle(String handle);

  boolean existsByEmail(String email);

  boolean existsByHandle(String handle);

  boolean existsByDocumentNumberHash(String documentNumberHash);
}
