package dev.civicpulse.payments.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.UUID;

public record CreatePaymentIntentRequest(
    @NotBlank String purpose,
    @NotNull UUID referenceId,
    @NotNull UUID payeeId,
    @Positive long amountCents,
    @NotBlank String gateway,
    @NotBlank String idempotencyKey) {}
