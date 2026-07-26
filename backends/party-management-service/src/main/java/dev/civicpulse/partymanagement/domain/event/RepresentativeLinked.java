package dev.civicpulse.partymanagement.domain.event;

import java.time.Instant;
import java.util.UUID;

/** Consumed by Directory Service to project office/role/state onto a politician (see
 * docs/db/directory-service/schema.sql). {@code govLevel} (one of directory-service's
 * {@code gov_level_options} codes — "federal"/"state"/"municipal") is nullable: the two
 * politician-admin flows (party self-registration, platform-admin reassignment) never set it and
 * directory-service leaves the projection's level untouched in that case; only
 * government-sync-service (see SyncPoliticianService) populates it. */
public record RepresentativeLinked(
    UUID partyId, UUID politicianAccountId, String roleTitle, String state, String govLevel, Instant occurredAt)
    implements DomainEvent {

  @Override
  public String topic() {
    return "representative-linked";
  }
}
