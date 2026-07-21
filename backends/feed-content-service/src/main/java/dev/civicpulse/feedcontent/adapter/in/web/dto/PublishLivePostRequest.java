package dev.civicpulse.feedcontent.adapter.in.web.dto;

import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

public record PublishLivePostRequest(
    @NotNull UUID liveSessionId,
    String imageUrl,
    String fileUrl,
    String fileName,
    List<String> pollOptions,
    String visibility,
    String context) {}
