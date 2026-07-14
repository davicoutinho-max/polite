package dev.civicpulse.identity.adapter.out.persistence;

import dev.civicpulse.identity.domain.model.AccountType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

interface RoleJpaRepository extends JpaRepository<RoleJpaEntity, Short> {

  Optional<RoleJpaEntity> findByName(AccountType name);
}
