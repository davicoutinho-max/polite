package dev.civicpulse.membershipaffiliation.adapter.in.web.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record RequestAffiliationRequest(@NotNull UUID partyId, String city) {}
