package dev.civicpulse.partymanagement.adapter.in.messaging.dto;

import java.time.Instant;
import java.util.UUID;

/** Local shape of membership-affiliation-service's {@code AffiliationRequested} — carries the
 * saga's own id ({@code affiliations.id}) so this service's review row and that service's
 * saga instance share the same identifier. Field name must be {@code occurredAt}, matching
 * every other event's DomainEvent convention (see directory-service's RepresentativeLinkedMessage
 * javadoc for why a mismatched field name silently deserializes to null instead of failing
 * loudly) — membership-affiliation-service's own event record must be built with this same
 * field name when that service is implemented. */
public record AffiliationRequestedMessage(UUID affiliationId, UUID partyId, UUID citizenAccountId, String city, Instant occurredAt) {}
