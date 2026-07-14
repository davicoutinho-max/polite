package dev.civicpulse.platformconfig.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;

public record SetTranslationRequest(@NotBlank String value) {}
