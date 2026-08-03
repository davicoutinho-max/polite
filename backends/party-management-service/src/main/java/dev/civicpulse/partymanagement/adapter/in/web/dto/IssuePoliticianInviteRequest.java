package dev.civicpulse.partymanagement.adapter.in.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record IssuePoliticianInviteRequest(@NotBlank String name, String roleTitle, String state, @NotBlank @Email String targetEmail) {}
