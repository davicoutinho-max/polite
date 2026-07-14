package dev.civicpulse.activityfeed.adapter.in.messaging.dto;

import java.time.Instant;
import java.util.UUID;

public record LegislativeItemStatusChangedMessage(UUID legislativeItemId, UUID politicianAccountId, String status, Instant occurredAt) {}
