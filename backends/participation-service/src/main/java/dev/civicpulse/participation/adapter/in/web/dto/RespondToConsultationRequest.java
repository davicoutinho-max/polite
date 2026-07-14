package dev.civicpulse.participation.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record RespondToConsultationRequest(@NotNull UUID citizenAccountId, @NotBlank String stance) {}
