package dev.civicpulse.identity.adapter.out.persistence;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

interface RolePermissionJpaRepository extends JpaRepository<RolePermissionJpaEntity, RolePermissionId> {

  List<RolePermissionJpaEntity> findByRoleId(Short roleId);
}
