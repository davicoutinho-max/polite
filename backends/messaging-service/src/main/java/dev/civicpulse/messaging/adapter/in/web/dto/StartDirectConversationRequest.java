package dev.civicpulse.messaging.adapter.in.web.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record StartDirectConversationRequest(@NotNull UUID otherAccountId) {}
