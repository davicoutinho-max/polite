package dev.civicpulse.partymanagement.adapter.in.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterPoliticianRequest(
    @NotBlank String name,
    @NotBlank String handle,
    @NotBlank @Email String email,
    @NotBlank @Size(min = 8) String password,
    @NotBlank String documentType,
    @NotBlank String documentNumber,
    String roleTitle) {}
