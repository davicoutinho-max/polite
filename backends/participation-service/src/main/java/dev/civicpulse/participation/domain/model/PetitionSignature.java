package dev.civicpulse.participation.domain.model;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/** A finalized, verified signature — only ever created once the matching {@link
 * PendingSignatureVerification} has been confirmed, so every instance here already represents a
 * completed identity check. Fields beyond petitionId/citizenAccountId/signedAt exist purely as
 * the legal audit trail (who signed, with what identifying data, when) — this is evidentiary
 * record-keeping, not a login credential, so it is kept as submitted rather than hashed. */
public final class PetitionSignature {

  private final UUID petitionId;
  private final UUID citizenAccountId;
  private final Instant signedAt;
  private final String fullName;
  private final String cpf;
  private final LocalDate birthDate;
  private final String city;
  private final String state;
  private final String verificationMethod;
  private final String electoralData;
  private final boolean eSignatureConsent;
  private final boolean identityValidated;
  private final String typedSignature;

  private PetitionSignature(
      UUID petitionId,
      UUID citizenAccountId,
      Instant signedAt,
      String fullName,
      String cpf,
      LocalDate birthDate,
      String city,
      String state,
      String verificationMethod,
      String electoralData,
      boolean eSignatureConsent,
      boolean identityValidated,
      String typedSignature) {
    this.petitionId = Objects.requireNonNull(petitionId);
    this.citizenAccountId = Objects.requireNonNull(citizenAccountId);
    this.signedAt = Objects.requireNonNull(signedAt);
    this.fullName = fullName;
    this.cpf = cpf;
    this.birthDate = birthDate;
    this.city = city;
    this.state = state;
    this.verificationMethod = verificationMethod;
    this.electoralData = electoralData;
    this.eSignatureConsent = eSignatureConsent;
    this.identityValidated = identityValidated;
    this.typedSignature = typedSignature;
  }

  public static PetitionSignature sign(
      UUID petitionId,
      UUID citizenAccountId,
      Instant now,
      String fullName,
      String cpf,
      LocalDate birthDate,
      String city,
      String state,
      String verificationMethod,
      String electoralData,
      boolean eSignatureConsent,
      boolean identityValidated,
      String typedSignature) {
    return new PetitionSignature(
        petitionId, citizenAccountId, now, fullName, cpf, birthDate, city, state, verificationMethod, electoralData, eSignatureConsent,
        identityValidated, typedSignature);
  }

  public static PetitionSignature reconstitute(
      UUID petitionId,
      UUID citizenAccountId,
      Instant signedAt,
      String fullName,
      String cpf,
      LocalDate birthDate,
      String city,
      String state,
      String verificationMethod,
      String electoralData,
      boolean eSignatureConsent,
      boolean identityValidated,
      String typedSignature) {
    return new PetitionSignature(
        petitionId, citizenAccountId, signedAt, fullName, cpf, birthDate, city, state, verificationMethod, electoralData, eSignatureConsent,
        identityValidated, typedSignature);
  }

  public UUID petitionId() {
    return petitionId;
  }

  public UUID citizenAccountId() {
    return citizenAccountId;
  }

  public Instant signedAt() {
    return signedAt;
  }

  public Optional<String> fullName() {
    return Optional.ofNullable(fullName);
  }

  public Optional<String> cpf() {
    return Optional.ofNullable(cpf);
  }

  public Optional<LocalDate> birthDate() {
    return Optional.ofNullable(birthDate);
  }

  public Optional<String> city() {
    return Optional.ofNullable(city);
  }

  public Optional<String> state() {
    return Optional.ofNullable(state);
  }

  public Optional<String> verificationMethod() {
    return Optional.ofNullable(verificationMethod);
  }

  public Optional<String> electoralData() {
    return Optional.ofNullable(electoralData);
  }

  public boolean eSignatureConsent() {
    return eSignatureConsent;
  }

  public boolean identityValidated() {
    return identityValidated;
  }

  public Optional<String> typedSignature() {
    return Optional.ofNullable(typedSignature);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof PetitionSignature other)) return false;
    return petitionId.equals(other.petitionId) && citizenAccountId.equals(other.citizenAccountId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(petitionId, citizenAccountId);
  }
}
