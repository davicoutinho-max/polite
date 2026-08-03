package dev.civicpulse.platformconfig.adapter.in.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record IssuePartyInviteRequest(
    @NotBlank String name,
    @NotBlank String acronym,
    @NotNull Integer number,
    String ideology,
    String president,
    @NotBlank String cnpj,
    @NotBlank @Email String targetEmail) {}
