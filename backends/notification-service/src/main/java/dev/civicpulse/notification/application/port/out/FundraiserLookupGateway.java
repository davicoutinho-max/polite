package dev.civicpulse.notification.application.port.out;

import java.util.Optional;
import java.util.UUID;

/** Anti-corruption-layer port to fundraising-service — a real synchronous REST call, used to
 * recover the organizer behind a {@code FundraiserGoalReached} event, which carries only the
 * fundraiser id (see NotificationServiceApplication's note). */
public interface FundraiserLookupGateway {

  Optional<UUID> lookupOrganizerAccountId(UUID fundraiserId);
}
