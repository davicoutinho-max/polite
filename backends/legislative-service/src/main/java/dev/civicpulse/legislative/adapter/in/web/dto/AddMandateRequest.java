package dev.civicpulse.legislative.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;

public record AddMandateRequest(@NotBlank String role, @NotBlank String period, boolean current) {}
