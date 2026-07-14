package dev.civicpulse.platformconfig.adapter.in.messaging.dto;

import java.time.Instant;
import java.util.UUID;

/** Local shape of party-management-service's {@code PoliticianRegistered} event record — field
 * names must match the producer's record component names exactly (see directory-service's
 * RepresentativeLinkedMessage javadoc for why a mismatch silently deserializes to null). */
public record PoliticianRegisteredMessage(UUID politicianAccountId, UUID partyId, String cpfHash, String cnpjHash, Instant occurredAt) {}
