package dev.civicpulse.feedcontent.adapter.in.web.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record PublishLivePostRequest(@NotNull UUID liveSessionId, String visibility, String context) {}
