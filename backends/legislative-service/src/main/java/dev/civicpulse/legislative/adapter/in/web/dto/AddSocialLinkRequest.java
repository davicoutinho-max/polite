package dev.civicpulse.legislative.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;

public record AddSocialLinkRequest(@NotBlank String platform, @NotBlank String label, @NotBlank String handle, @NotBlank String url) {}
