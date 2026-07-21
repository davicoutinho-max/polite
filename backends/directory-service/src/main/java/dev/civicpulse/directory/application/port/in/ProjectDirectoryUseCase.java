package dev.civicpulse.directory.application.port.in;

import java.time.Instant;
import java.util.UUID;

/** Consumes the events listed in docs/db/directory-service/schema.sql's "Domain events
 * consumed" section and keeps the {@code politicians}/{@code parties} projections up to date.
 * Called only from the inbound Kafka adapter — never from a web controller. */
public interface ProjectDirectoryUseCase {

  /** {@code AccountRegistered} — the only source of name/handle/avatarUrl, fetched via
   * {@link dev.civicpulse.directory.application.port.out.AccountLookupGateway} since the event
   * itself doesn't carry them. No-ops for non-politician account types. */
  void onAccountRegistered(UUID accountId, String accountType);

  /** {@code RepresentativeLinked} — carries the office/role title, state, and party linkage. */
  void onRepresentativeLinked(UUID politicianAccountId, UUID partyId, String roleTitle, String state, Instant linkedAt);

  /** {@code PoliticianRegistered} — published by Party Management at registration time;
   * updates the party linkage if the projection already exists. */
  void onPoliticianRegistered(UUID politicianAccountId, UUID partyId, Instant occurredAt);

  /** {@code PoliticianReassigned} — published by Platform Configuration on admin reassignment. */
  void onPoliticianReassigned(UUID politicianAccountId, UUID partyId, Instant updatedAt);

  /** {@code PartyRegistered} — published by Platform Configuration; projects the public party
   * catalog entry. */
  void onPartyRegistered(
      UUID partyId, String name, String acronym, int number, String president, String ideology, Instant occurredAt);
}
