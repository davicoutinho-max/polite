package dev.civicpulse.identity.adapter.out.persistence;

import java.io.Serializable;
import java.util.Objects;

/** Composite key for {@code role_permissions (role_id, permission)}. */
public class RolePermissionId implements Serializable {

  private Short roleId;
  private String permission;

  public RolePermissionId() {}

  public RolePermissionId(Short roleId, String permission) {
    this.roleId = roleId;
    this.permission = permission;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof RolePermissionId that)) return false;
    return Objects.equals(roleId, that.roleId) && Objects.equals(permission, that.permission);
  }

  @Override
  public int hashCode() {
    return Objects.hash(roleId, permission);
  }
}
