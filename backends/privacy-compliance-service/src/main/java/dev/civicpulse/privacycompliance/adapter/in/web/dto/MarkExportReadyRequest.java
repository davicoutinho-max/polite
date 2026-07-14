package dev.civicpulse.privacycompliance.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import java.time.Instant;

public record MarkExportReadyRequest(@NotBlank String downloadUrl, Instant expiresAt) {}
