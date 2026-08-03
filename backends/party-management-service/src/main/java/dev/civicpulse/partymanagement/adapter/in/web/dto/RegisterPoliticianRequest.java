package dev.civicpulse.partymanagement.adapter.in.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Registering a politician redeems a party-issued invite token (see
 * ManagePoliticianInviteUseCase) — name/role/state come from the token, not this request, see
 * RegisterPoliticianUseCase's javadoc for why. */
public record RegisterPoliticianRequest(
    @NotBlank String registrationToken,
    @NotBlank String handle,
    @NotBlank @Email String email,
    @NotBlank @Size(min = 8) String password,
    @NotBlank String documentType,
    @NotBlank String documentNumber) {}
