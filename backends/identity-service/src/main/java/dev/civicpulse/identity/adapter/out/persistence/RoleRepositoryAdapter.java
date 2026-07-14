package dev.civicpulse.identity.adapter.out.persistence;

import dev.civicpulse.identity.application.port.out.RoleRepository;
import dev.civicpulse.identity.domain.model.AccountType;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
class RoleRepositoryAdapter implements RoleRepository {

  private final RoleJpaRepository roleJpaRepository;
  private final RolePermissionJpaRepository rolePermissionJpaRepository;

  RoleRepositoryAdapter(RoleJpaRepository roleJpaRepository, RolePermissionJpaRepository rolePermissionJpaRepository) {
    this.roleJpaRepository = roleJpaRepository;
    this.rolePermissionJpaRepository = rolePermissionJpaRepository;
  }

  @Override
  public Set<String> findPermissionsByAccountType(AccountType accountType) {
    return roleJpaRepository
        .findByName(accountType)
        .map(
            role ->
                rolePermissionJpaRepository.findByRoleId(role.getId()).stream()
                    .map(RolePermissionJpaEntity::getPermission)
                    .collect(Collectors.toUnmodifiableSet()))
        .orElseGet(Set::of);
  }
}
