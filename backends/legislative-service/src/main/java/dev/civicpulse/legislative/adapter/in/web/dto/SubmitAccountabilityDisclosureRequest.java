package dev.civicpulse.legislative.adapter.in.web.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record SubmitAccountabilityDisclosureRequest(
    @NotBlank String category,
    @Min(1) @Max(12) int periodMonth,
    @Min(2000) int periodYear,
    @PositiveOrZero @NotNull Long declaredAmountCents,
    @NotBlank String documentUrl,
    String notes) {}
