package dev.civicpulse.membershipaffiliation.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;

public record GenerateFeeRequest(@NotBlank String referencePeriod, @Positive long amountCents, @NotNull LocalDate dueDate) {}
