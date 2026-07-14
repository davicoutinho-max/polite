package dev.civicpulse.feedcontent.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;

public record AddTagRequest(@NotBlank String label, String severity, String icon) {}
