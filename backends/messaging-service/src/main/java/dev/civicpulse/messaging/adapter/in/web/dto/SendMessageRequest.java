package dev.civicpulse.messaging.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;

public record SendMessageRequest(@NotBlank String body) {}
