package dev.civicpulse.participation.adapter.in.web.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record CastVoteRequest(@NotNull UUID citizenAccountId, @NotNull UUID optionId) {}
