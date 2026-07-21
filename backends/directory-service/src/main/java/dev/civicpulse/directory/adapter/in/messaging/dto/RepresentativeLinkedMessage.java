package dev.civicpulse.directory.adapter.in.messaging.dto;

import java.time.Instant;
import java.util.UUID;

/** Local shape of party-management-service's {@code RepresentativeLinked} event record —
 * field names must match the producer's record component names exactly (Jackson matches by
 * property name; a mismatch silently deserializes to null rather than failing loudly). The
 * producer's event is {@code RepresentativeLinked(partyId, politicianAccountId, roleTitle,
 * state, occurredAt)}, not the {@code party_representatives.linked_at} column name. */
public record RepresentativeLinkedMessage(UUID partyId, UUID politicianAccountId, String roleTitle, String state, Instant occurredAt) {}
