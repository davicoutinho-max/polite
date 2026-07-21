package dev.civicpulse.notification.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import java.util.UUID;

public record BroadcastNotificationRequest(
    @NotEmpty List<UUID> recipientAccountIds, @NotBlank String category, String icon, @NotBlank String title, @NotBlank String message, String link) {}
