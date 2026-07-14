package dev.civicpulse.livestreaming.adapter.in.web.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record RecordViewerCountRequest(@NotNull @Min(0) Integer currentViewers) {}
