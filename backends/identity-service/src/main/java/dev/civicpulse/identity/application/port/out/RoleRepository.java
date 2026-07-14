package dev.civicpulse.identity.application.port.out;

import dev.civicpulse.identity.domain.model.AccountType;
import java.util.Set;

/** Resolves a role's permission set from {@code roles}/{@code role_permissions} — the editable,
 * data-driven replacement for the frontend's static TYPE_PERMISSIONS map. */
public interface RoleRepository {

  Set<String> findPermissionsByAccountType(AccountType accountType);
}
