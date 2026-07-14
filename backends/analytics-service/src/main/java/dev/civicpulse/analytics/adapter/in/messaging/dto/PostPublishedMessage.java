package dev.civicpulse.analytics.adapter.in.messaging.dto;

import java.time.Instant;
import java.util.UUID;

public record PostPublishedMessage(UUID postId, UUID authorId, String kind, String visibility, Instant occurredAt) {}
