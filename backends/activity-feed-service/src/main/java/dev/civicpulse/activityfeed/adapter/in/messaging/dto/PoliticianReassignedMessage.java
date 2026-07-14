package dev.civicpulse.activityfeed.adapter.in.messaging.dto;

import java.time.Instant;
import java.util.UUID;

public record PoliticianReassignedMessage(UUID politicianAccountId, UUID partyId, Instant occurredAt) {}
