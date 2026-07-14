package dev.civicpulse.identity.adapter.in.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Public self-registration payload — always creates a {@code citizen} account. Mirrors the
 * Angular app's register-page.ts fields exactly (name, email, CPF, password). */
public record RegisterAccountRequest(
    @NotBlank String name,
    @NotBlank String handle,
    @NotBlank @Email String email,
    @NotBlank @Size(min = 8, message = "password must be at least 8 characters") String password,
    @NotBlank String cpf) {}
