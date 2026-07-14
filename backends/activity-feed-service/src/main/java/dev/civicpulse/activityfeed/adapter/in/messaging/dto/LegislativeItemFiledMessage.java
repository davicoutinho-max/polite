package dev.civicpulse.activityfeed.adapter.in.messaging.dto;

import java.time.Instant;
import java.util.UUID;

public record LegislativeItemFiledMessage(UUID legislativeItemId, UUID politicianAccountId, String category, String reference, Instant occurredAt) {}
