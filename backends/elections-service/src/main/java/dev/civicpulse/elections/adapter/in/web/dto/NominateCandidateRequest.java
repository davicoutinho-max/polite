package dev.civicpulse.elections.adapter.in.web.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record NominateCandidateRequest(@NotNull UUID politicianAccountId) {}
