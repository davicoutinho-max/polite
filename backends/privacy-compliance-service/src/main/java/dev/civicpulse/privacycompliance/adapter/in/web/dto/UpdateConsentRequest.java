package dev.civicpulse.privacycompliance.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UpdateConsentRequest(@NotBlank String purpose, @NotNull Boolean granted) {}
