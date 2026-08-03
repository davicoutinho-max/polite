package dev.civicpulse.platformconfig.adapter.in.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Registering a party redeems an admin-issued invite token (see ManagePartyInviteUseCase) and
 * provisions its authenticatable identity account in one step — the returned party id and the
 * account id are the same UUID everywhere in the platform (see RegisterPartyService's scope
 * note). The party's name/acronym/number/ideology/president/CNPJ all come from the token, not
 * this request — see RegisterPartyUseCase's javadoc for why. */
public record RegisterPartyRequest(
    @NotBlank String registrationToken,
    @NotBlank String handle,
    @NotBlank @Email String email,
    @NotBlank @Size(min = 8, message = "password must be at least 8 characters") String password) {}
