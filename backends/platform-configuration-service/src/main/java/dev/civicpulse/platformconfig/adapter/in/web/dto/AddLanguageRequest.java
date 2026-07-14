package dev.civicpulse.platformconfig.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;

public record AddLanguageRequest(@NotBlank String id, @NotBlank String name, @NotBlank String code, boolean isDefault) {}
