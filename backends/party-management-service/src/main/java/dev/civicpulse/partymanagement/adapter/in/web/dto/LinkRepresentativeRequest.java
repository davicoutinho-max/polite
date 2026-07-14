package dev.civicpulse.partymanagement.adapter.in.web.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record LinkRepresentativeRequest(@NotNull UUID politicianAccountId, String roleTitle) {}
