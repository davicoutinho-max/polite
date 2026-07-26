package dev.civicpulse.participation.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.UUID;

public record StartSignatureRequest(
    @NotNull UUID citizenAccountId,
    @NotBlank String fullName,
    @NotBlank String cpf,
    LocalDate birthDate,
    String city,
    String state,
    @NotBlank String verificationMethod,
    String contact,
    String electoralData,
    boolean eSignatureConsent,
    @NotBlank String typedSignature) {}
