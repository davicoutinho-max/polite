package dev.civicpulse.activityfeed.adapter.in.messaging.dto;

import java.time.Instant;
import java.util.UUID;

public record VoteCastMessage(UUID voteRecordId, UUID politicianAccountId, String matter, String choice, Instant occurredAt) {}
