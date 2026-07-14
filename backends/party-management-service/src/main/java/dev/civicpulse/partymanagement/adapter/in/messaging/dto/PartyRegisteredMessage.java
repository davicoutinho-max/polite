package dev.civicpulse.partymanagement.adapter.in.messaging.dto;

import java.time.Instant;
import java.util.UUID;

/** Local shape of platform-configuration-service's {@code PartyRegistered} — see
 * directory-service's identical DTO for the field rationale (mirrors {@code party_registry}). */
public record PartyRegisteredMessage(
    UUID partyId, String name, String acronym, int number, String president, String ideology, Instant occurredAt) {}
