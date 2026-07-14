package dev.civicpulse.directory.application.port.out;

import java.util.Optional;
import java.util.UUID;

/** Anti-corruption-layer boundary: Identity's {@code AccountRegistered} event (see
 * docs/db/identity-service/schema.sql) carries only {@code account_id/account_type/
 * document_hash} — no name/handle. Rather than growing Identity's event payload or coupling
 * Directory to Identity's database, Directory enriches its projection with a read-only call to
 * Identity's public REST API (not its database) the moment it learns a new politician/party
 * account exists. See docs/architecture/system-architecture.html's Communication Patterns
 * table — API calls for read-enrichment, Kafka for cross-service consistency. */
public interface AccountLookupGateway {

  Optional<AccountSummary> findAccount(UUID accountId);

  record AccountSummary(UUID accountId, String name, String handle, String avatarUrl) {}
}
