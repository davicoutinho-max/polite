package dev.civicpulse.platformconfig.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;

/** Internal-only, called by government-sync-service — see SyncPartyUseCase. */
public record SyncPartyRequest(
    @NotBlank String name,
    @NotBlank String acronym,
    int number,
    String logoUrl,
    @NotBlank String documentType,
    @NotBlank String documentNumber,
    @NotBlank String externalSource,
    @NotBlank String externalId) {}
