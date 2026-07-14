package dev.civicpulse.notification.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;

public record RegisterPushTokenRequest(@NotBlank String platform, @NotBlank String token) {}
