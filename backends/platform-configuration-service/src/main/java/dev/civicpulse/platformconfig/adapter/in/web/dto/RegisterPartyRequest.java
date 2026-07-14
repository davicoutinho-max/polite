package dev.civicpulse.platformconfig.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RegisterPartyRequest(@NotBlank String name, @NotBlank String acronym, @NotNull Integer number, String president, String ideology) {}
