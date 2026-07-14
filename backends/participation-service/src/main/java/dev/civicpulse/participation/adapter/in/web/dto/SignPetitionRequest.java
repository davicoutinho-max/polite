package dev.civicpulse.participation.adapter.in.web.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record SignPetitionRequest(@NotNull UUID citizenAccountId) {}
