package dev.civicpulse.feedcontent.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import java.time.Instant;
import java.util.List;

public record PublishTextPostRequest(
    @NotBlank String content,
    String imageUrl,
    String fileUrl,
    String fileName,
    List<String> pollOptions,
    Instant pollClosesAt,
    String visibility,
    String context) {}
