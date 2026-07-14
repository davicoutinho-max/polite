package dev.civicpulse.notification.application.port.out;

import java.util.Optional;
import java.util.UUID;

/** Anti-corruption-layer port to membership-affiliation-service — a real synchronous REST call
 * (see party-management-service's IdentityProvisioningGateway for the established pattern), used
 * to recover the citizen behind a {@code MembershipFeeGenerated}/{@code MembershipFeeOverdue}
 * event, which carries only the fee/affiliation id (see NotificationServiceApplication's note). */
public interface AffiliationLookupGateway {

  Optional<UUID> lookupCitizenAccountId(UUID affiliationId);
}
