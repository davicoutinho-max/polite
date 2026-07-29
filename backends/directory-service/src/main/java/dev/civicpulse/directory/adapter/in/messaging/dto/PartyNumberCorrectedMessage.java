package dev.civicpulse.directory.adapter.in.messaging.dto;

import java.time.Instant;
import java.util.UUID;

/** Local shape of platform-configuration-service's {@code PartyNumberCorrected} event record —
 * see that record's javadoc for why this exists (a party first registered with a synthetic
 * placeholder number later getting the real TSE number). Field names must match the producer's
 * record component names exactly (Jackson matches by property name). */
public record PartyNumberCorrectedMessage(UUID partyId, int number, Instant occurredAt) {}
