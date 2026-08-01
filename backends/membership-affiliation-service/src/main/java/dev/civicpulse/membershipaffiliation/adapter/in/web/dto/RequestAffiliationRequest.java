package dev.civicpulse.membershipaffiliation.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record RequestAffiliationRequest(
    @NotNull UUID partyId,
    String city,
    @NotBlank String voterRegistrationNumber,
    @NotBlank String electoralZone,
    @NotBlank String electoralSection,
    @NotBlank String electoralState,
    @NotBlank String electoralMunicipality,
    @NotBlank String identityPhotoUrl) {}
