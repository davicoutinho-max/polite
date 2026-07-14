package dev.civicpulse.legislative.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;

public record JoinCommitteeRequest(@NotBlank String name, @NotBlank String role, @NotBlank String kind) {}
