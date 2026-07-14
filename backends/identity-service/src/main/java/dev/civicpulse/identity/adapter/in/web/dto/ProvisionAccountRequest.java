package dev.civicpulse.identity.adapter.in.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Internal provisioning payload — called by Party Management when a party registers a
 * politician, or by a platform-admin flow. Not exposed to the public internet by the
 * Gateway's routing table (see docs/architecture/system-architecture.html). */
public record ProvisionAccountRequest(
    @NotNull String accountType,
    @NotBlank String name,
    @NotBlank String handle,
    @NotBlank @Email String email,
    @NotBlank @Size(min = 8) String password,
    @NotBlank String documentType,
    @NotBlank String documentNumber) {}
