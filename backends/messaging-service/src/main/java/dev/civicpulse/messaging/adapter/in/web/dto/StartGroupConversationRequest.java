package dev.civicpulse.messaging.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import java.util.UUID;

public record StartGroupConversationRequest(@NotEmpty List<UUID> participantAccountIds, @NotBlank String groupName, String groupAvatarUrl) {}
