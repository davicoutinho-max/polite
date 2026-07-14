package dev.civicpulse.directory.adapter.in.messaging.dto;

import java.time.Instant;
import java.util.UUID;

/** Local shape of platform-configuration-service's {@code PartyRegistered} — mirrors
 * {@code party_registry(id, name, acronym, number, president, ideology)}. */
public record PartyRegisteredMessage(
    UUID partyId, String name, String acronym, int number, String president, String ideology, Instant occurredAt) {}
