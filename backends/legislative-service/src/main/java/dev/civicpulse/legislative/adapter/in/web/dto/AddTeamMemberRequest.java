package dev.civicpulse.legislative.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;

public record AddTeamMemberRequest(@NotBlank String name, @NotBlank String role, String avatarUrl) {}
