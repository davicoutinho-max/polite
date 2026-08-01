package dev.civicpulse.assistant.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record GenerateSocialVariantsRequest(@NotBlank @Size(max = 3000) String postText) {}
