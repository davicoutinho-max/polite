package dev.civicpulse.legislative.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AddExpenseLineRequest(@NotBlank String category, @NotNull Long amountCents) {}
