package dev.civicpulse.partymanagement.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

/** Internal-only, called by government-sync-service — see SyncPoliticianUseCase. */
public record SyncPoliticianRequest(
    @NotNull UUID partyId,
    @NotBlank String name,
    @NotBlank String handle,
    @NotBlank String email,
    String avatarUrl,
    @NotBlank String documentType,
    @NotBlank String documentNumber,
    @NotBlank String externalSource,
    @NotBlank String externalId,
    String roleTitle,
    String state,
    String govLevel) {}
