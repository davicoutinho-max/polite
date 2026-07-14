package dev.civicpulse.activityfeed.adapter.in.messaging.dto;

import java.time.Instant;
import java.util.UUID;

public record CommitteeMembershipChangedMessage(UUID committeeMembershipId, UUID politicianAccountId, String name, Instant occurredAt) {}
