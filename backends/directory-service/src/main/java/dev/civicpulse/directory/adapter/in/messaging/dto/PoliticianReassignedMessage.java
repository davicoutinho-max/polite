package dev.civicpulse.directory.adapter.in.messaging.dto;

import java.time.Instant;
import java.util.UUID;

/** Local shape of platform-configuration-service's {@code PoliticianReassigned} event record.
 * Field name must match the producer's record component name exactly — see
 * RepresentativeLinkedMessage's javadoc for why {@code occurredAt} (the DomainEvent
 * convention), not the {@code politician_assignments.updated_at} column name. */
public record PoliticianReassignedMessage(UUID politicianAccountId, UUID partyId, Instant occurredAt) {}
