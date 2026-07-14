package dev.civicpulse.assistant.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.UUID;

public record CreateTopicRequest(@NotBlank String reference, @NotBlank String title, UUID legislativeItemId) {}
