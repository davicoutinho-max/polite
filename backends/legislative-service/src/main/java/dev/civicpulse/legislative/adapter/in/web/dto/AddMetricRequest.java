package dev.civicpulse.legislative.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AddMetricRequest(String icon, @NotBlank String label, @NotNull Long valueCents, String caption, @NotBlank String period) {}
