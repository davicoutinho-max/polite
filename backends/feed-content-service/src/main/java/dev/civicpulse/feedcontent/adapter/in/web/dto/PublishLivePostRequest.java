package dev.civicpulse.feedcontent.adapter.in.web.dto;

import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record PublishLivePostRequest(
    @NotNull UUID liveSessionId,
    String imageUrl,
    String fileUrl,
    String fileName,
    List<String> pollOptions,
    Instant pollClosesAt,
    String visibility,
    String context) {}
