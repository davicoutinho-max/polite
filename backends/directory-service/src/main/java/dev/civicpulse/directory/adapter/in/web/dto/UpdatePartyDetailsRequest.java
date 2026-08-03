package dev.civicpulse.directory.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdatePartyDetailsRequest(
    @NotBlank String name, @NotBlank String acronym, int number, String ideology, Integer foundedYear, String president) {}
