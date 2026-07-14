package dev.civicpulse.feedcontent.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;

public record PublishTextPostRequest(@NotBlank String content, String imageUrl, String visibility, String context) {}
