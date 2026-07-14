package dev.civicpulse.platformconfig.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AddCountryRequest(@NotBlank String name, @NotBlank @Size(min = 2, max = 2) String code) {}
