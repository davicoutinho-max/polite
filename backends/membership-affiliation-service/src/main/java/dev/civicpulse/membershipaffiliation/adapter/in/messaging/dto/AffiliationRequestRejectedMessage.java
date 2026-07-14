package dev.civicpulse.membershipaffiliation.adapter.in.messaging.dto;

import java.time.Instant;
import java.util.UUID;

public record AffiliationRequestRejectedMessage(UUID requestId, UUID partyId, UUID citizenAccountId, Instant occurredAt) {}
