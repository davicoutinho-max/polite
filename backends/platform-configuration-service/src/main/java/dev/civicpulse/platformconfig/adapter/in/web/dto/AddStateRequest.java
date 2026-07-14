package dev.civicpulse.platformconfig.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;

public record AddStateRequest(@NotBlank String name, @NotBlank String code) {}
