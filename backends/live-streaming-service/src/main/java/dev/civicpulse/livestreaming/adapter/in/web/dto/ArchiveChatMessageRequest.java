package dev.civicpulse.livestreaming.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record ArchiveChatMessageRequest(@NotNull UUID accountId, @NotBlank String body) {}
