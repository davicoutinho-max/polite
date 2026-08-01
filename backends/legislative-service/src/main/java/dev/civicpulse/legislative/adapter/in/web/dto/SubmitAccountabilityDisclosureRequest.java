package dev.civicpulse.legislative.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record SubmitAccountabilityDisclosureRequest(
    @NotBlank String category, @PositiveOrZero @NotNull Long declaredAmountCents, @NotBlank String documentUrl) {}
