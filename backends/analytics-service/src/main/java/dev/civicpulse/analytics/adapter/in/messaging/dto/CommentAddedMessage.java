package dev.civicpulse.analytics.adapter.in.messaging.dto;

import java.time.Instant;
import java.util.UUID;

public record CommentAddedMessage(UUID postId, UUID commentId, Instant occurredAt) {}
