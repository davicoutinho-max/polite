package dev.civicpulse.platformconfig.adapter.in.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Registering a party also provisions its authenticatable identity account in one step — the
 * returned party id and the account id are the same UUID everywhere in the platform (see
 * RegisterPartyService's scope note). */
public record RegisterPartyRequest(
    @NotBlank String name,
    @NotBlank String acronym,
    @NotNull Integer number,
    String president,
    String ideology,
    @NotBlank String handle,
    @NotBlank @Email String email,
    @NotBlank @Size(min = 8, message = "password must be at least 8 characters") String password,
    @NotBlank String documentType,
    @NotBlank String documentNumber) {}
