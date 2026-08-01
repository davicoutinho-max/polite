package dev.civicpulse.assistant.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record VerifyDocumentRequest(
    @NotBlank String documentUrl, @NotBlank String categoryLabel, @PositiveOrZero @NotNull Long declaredAmountCents) {}
