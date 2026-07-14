package dev.civicpulse.identity.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

@Entity
@Table(name = "role_permissions")
@IdClass(RolePermissionId.class)
public class RolePermissionJpaEntity {

  @Id
  @Column(name = "role_id")
  private Short roleId;

  @Id private String permission;

  protected RolePermissionJpaEntity() {}

  public RolePermissionJpaEntity(Short roleId, String permission) {
    this.roleId = roleId;
    this.permission = permission;
  }

  public Short getRoleId() {
    return roleId;
  }

  public String getPermission() {
    return permission;
  }
}
