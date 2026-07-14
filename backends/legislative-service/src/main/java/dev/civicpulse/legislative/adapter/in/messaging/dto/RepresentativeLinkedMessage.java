package dev.civicpulse.legislative.adapter.in.messaging.dto;

import java.time.Instant;
import java.util.UUID;

/** Local shape of party-management-service's {@code RepresentativeLinked} — field names must
 * match the producer's record component names exactly (Jackson matches by property name). */
public record RepresentativeLinkedMessage(UUID partyId, UUID politicianAccountId, String roleTitle, Instant occurredAt) {}
