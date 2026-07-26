package dev.civicpulse.participation.domain.model;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/** The "start signature" step's result — everything the citizen submitted, held until the code
 * they receive (by SMS/email for Apoio Verificado, or the identity-check pass for Iniciativa
 * Popular) is confirmed. Never becomes a real {@link PetitionSignature} until then. There is no
 * real SMS/email gateway anywhere in this system (same as every other external-provider stub in
 * this codebase, e.g. identity-service's document verification) — {@code code} is generated here
 * and handed back to the caller for display, clearly marked as demo-only. */
public final class PendingSignatureVerification {

  private final UUID id;
  private final UUID petitionId;
  private final UUID citizenAccountId;
  private final String code;
  private final String contact;
  private final String method;
  private final String fullName;
  private final String cpf;
  private final LocalDate birthDate;
  private final String city;
  private final String state;
  private final String electoralData;
  private final boolean eSignatureConsent;
  private final String typedSignature;
  private final Instant expiresAt;
  private boolean consumed;

  private PendingSignatureVerification(
      UUID id,
      UUID petitionId,
      UUID citizenAccountId,
      String code,
      String contact,
      String method,
      String fullName,
      String cpf,
      LocalDate birthDate,
      String city,
      String state,
      String electoralData,
      boolean eSignatureConsent,
      String typedSignature,
      Instant expiresAt,
      boolean consumed) {
    this.id = Objects.requireNonNull(id);
    this.petitionId = Objects.requireNonNull(petitionId);
    this.citizenAccountId = Objects.requireNonNull(citizenAccountId);
    this.code = Objects.requireNonNull(code);
    this.contact = contact;
    this.method = Objects.requireNonNull(method);
    this.fullName = requireNonBlank(fullName, "fullName");
    this.cpf = requireNonBlank(cpf, "cpf");
    this.birthDate = birthDate;
    this.city = city;
    this.state = state;
    this.electoralData = electoralData;
    this.eSignatureConsent = eSignatureConsent;
    this.typedSignature = requireNonBlank(typedSignature, "typedSignature");
    this.expiresAt = Objects.requireNonNull(expiresAt);
    this.consumed = consumed;
  }

  public static PendingSignatureVerification create(
      UUID id,
      UUID petitionId,
      UUID citizenAccountId,
      String code,
      String contact,
      String method,
      String fullName,
      String cpf,
      LocalDate birthDate,
      String city,
      String state,
      String electoralData,
      boolean eSignatureConsent,
      String typedSignature,
      Instant expiresAt) {
    return new PendingSignatureVerification(
        id, petitionId, citizenAccountId, code, contact, method, fullName, cpf, birthDate, city, state, electoralData, eSignatureConsent,
        typedSignature, expiresAt, false);
  }

  public static PendingSignatureVerification reconstitute(
      UUID id,
      UUID petitionId,
      UUID citizenAccountId,
      String code,
      String contact,
      String method,
      String fullName,
      String cpf,
      LocalDate birthDate,
      String city,
      String state,
      String electoralData,
      boolean eSignatureConsent,
      String typedSignature,
      Instant expiresAt,
      boolean consumed) {
    return new PendingSignatureVerification(
        id, petitionId, citizenAccountId, code, contact, method, fullName, cpf, birthDate, city, state, electoralData, eSignatureConsent,
        typedSignature, expiresAt, consumed);
  }

  private static String requireNonBlank(String value, String field) {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException(field + " must not be blank");
    }
    return value;
  }

  public boolean matches(String candidateCode) {
    return code.equals(candidateCode);
  }

  public boolean isExpired(Instant now) {
    return !expiresAt.isAfter(now);
  }

  public boolean isConsumed() {
    return consumed;
  }

  public void consume() {
    this.consumed = true;
  }

  public UUID id() {
    return id;
  }

  public UUID petitionId() {
    return petitionId;
  }

  public UUID citizenAccountId() {
    return citizenAccountId;
  }

  public String code() {
    return code;
  }

  public Optional<String> contact() {
    return Optional.ofNullable(contact);
  }

  public String method() {
    return method;
  }

  public String fullName() {
    return fullName;
  }

  public String cpf() {
    return cpf;
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

  public Optional<String> electoralData() {
    return Optional.ofNullable(electoralData);
  }

  public boolean eSignatureConsent() {
    return eSignatureConsent;
  }

  public String typedSignature() {
    return typedSignature;
  }

  public Instant expiresAt() {
    return expiresAt;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof PendingSignatureVerification other)) return false;
    return id.equals(other.id);
  }

  @Override
  public int hashCode() {
    return id.hashCode();
  }
}
