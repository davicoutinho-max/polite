package dev.civicpulse.membershipaffiliation.adapter.in.messaging.dto;

import java.time.Instant;
import java.util.UUID;

/** Local shape of party-management-service's {@code AffiliationRequestApproved} event record
 * — field names must match the producer's record component names exactly (see
 * directory-service's RepresentativeLinkedMessage javadoc for why a mismatch silently
 * deserializes to null). {@code requestId} is the same id as this service's own {@code
 * affiliations.id}. */
public record AffiliationRequestApprovedMessage(UUID requestId, UUID partyId, UUID citizenAccountId, Instant occurredAt) {}
