package dev.civicpulse.identity.adapter.in.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.UUID;

/** Public self-registration payload — mirrors the Angular app's register-page.ts fields
 * (name, email, password, and one document field accepting either a CPF or a CNPJ; the type is
 * inferred from digit count, see DocumentType.fromDigitCount). {@code claimAccountId} is set only
 * when the citizen confirmed, earlier in the registration flow (via check-document or a
 * directory-search pick), that a specific unclaimed government-sourced profile is really them. */
public record RegisterAccountRequest(
    @NotBlank String name,
    @NotBlank String handle,
    @NotBlank @Email String email,
    @NotBlank @Size(min = 8, message = "password must be at least 8 characters") String password,
    @NotBlank String documentNumber,
    UUID claimAccountId) {}
