package dev.civicpulse.analytics.adapter.in.messaging.dto;

import java.time.Instant;
import java.util.UUID;

public record FollowCreatedMessage(UUID followerAccountId, String targetType, UUID targetId, Instant occurredAt) {}
