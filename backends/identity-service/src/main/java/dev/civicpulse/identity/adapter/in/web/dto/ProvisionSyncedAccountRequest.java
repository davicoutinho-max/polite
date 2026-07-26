package dev.civicpulse.identity.adapter.in.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/** Internal-only, called by government-sync-service — see ProvisionSyncedAccountUseCase. Not
 * exposed to the public internet by the Gateway's routing table, same as {@code /provision}. */
public record ProvisionSyncedAccountRequest(
    @NotNull String accountType,
    @NotBlank String name,
    @NotBlank String handle,
    @NotBlank @Email String email,
    String avatarUrl,
    @NotBlank String documentType,
    @NotBlank String documentNumber,
    @NotBlank String externalSource,
    @NotBlank String externalId) {}
