package dev.civicpulse.partymanagement.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;

public record AddOfficeRequest(@NotBlank String scope, @NotBlank String location, String leaderName) {}
