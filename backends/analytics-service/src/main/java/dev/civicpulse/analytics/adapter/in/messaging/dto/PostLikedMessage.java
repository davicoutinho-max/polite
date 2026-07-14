package dev.civicpulse.analytics.adapter.in.messaging.dto;

import java.time.Instant;
import java.util.UUID;

public record PostLikedMessage(UUID postId, UUID accountId, Instant occurredAt) {}
