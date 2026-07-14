package dev.civicpulse.payments.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;

public record RegisterPaymentMethodRequest(@NotBlank String type, @NotBlank String tokenRef) {}
