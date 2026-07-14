package dev.civicpulse.legislative.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;

public record AdvanceStatusRequest(@NotBlank String status) {}
