package dev.civicpulse.directory.adapter.in.messaging.dto;

import java.time.Instant;
import java.util.UUID;

/** Local shape of party-management-service's {@code RepresentativeLinked} event record —
 * field names must match the producer's record component names exactly (Jackson matches by
 * property name; a mismatch silently deserializes to null rather than failing loudly). The
 * producer's event is {@code RepresentativeLinked(partyId, politicianAccountId, roleTitle,
 * state, govLevel, occurredAt)}, not the {@code party_representatives.linked_at} column name.
 * {@code govLevel} is null for the two admin-driven flows (party self-registration, platform-
 * admin reassignment) — only government-sync-service populates it. */
public record RepresentativeLinkedMessage(
    UUID partyId, UUID politicianAccountId, String roleTitle, String state, String govLevel, Instant occurredAt) {}
