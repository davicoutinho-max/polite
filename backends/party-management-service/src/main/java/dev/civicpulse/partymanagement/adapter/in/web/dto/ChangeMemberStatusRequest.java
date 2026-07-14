package dev.civicpulse.partymanagement.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;

public record ChangeMemberStatusRequest(@NotBlank String status) {}
