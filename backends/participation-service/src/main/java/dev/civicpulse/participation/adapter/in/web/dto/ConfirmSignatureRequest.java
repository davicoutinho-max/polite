package dev.civicpulse.participation.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record ConfirmSignatureRequest(@NotNull UUID citizenAccountId, @NotNull UUID verificationId, @NotBlank String code) {}
