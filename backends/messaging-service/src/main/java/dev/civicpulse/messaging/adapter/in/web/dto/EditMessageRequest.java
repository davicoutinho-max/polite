package dev.civicpulse.messaging.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;

public record EditMessageRequest(@NotBlank String body) {}
