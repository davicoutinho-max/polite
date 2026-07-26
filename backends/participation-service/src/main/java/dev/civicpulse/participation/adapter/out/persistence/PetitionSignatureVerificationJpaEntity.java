package dev.civicpulse.participation.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "petition_signature_verifications")
public class PetitionSignatureVerificationJpaEntity {

  @Id private UUID id;

  @Column(name = "petition_id", nullable = false)
  private UUID petitionId;

  @Column(name = "citizen_account_id", nullable = false)
  private UUID citizenAccountId;

  @Column(nullable = false)
  private String code;

  private String contact;

  @Column(nullable = false)
  private String method;

  @Column(name = "full_name", nullable = false)
  private String fullName;

  @Column(nullable = false)
  private String cpf;

  @Column(name = "birth_date")
  private LocalDate birthDate;

  private String city;

  private String state;

  @Column(name = "electoral_data")
  private String electoralData;

  @Column(name = "e_signature_consent", nullable = false)
  private boolean eSignatureConsent;

  @Column(name = "typed_signature", nullable = false)
  private String typedSignature;

  @Column(name = "expires_at", nullable = false)
  private Instant expiresAt;

  @Column(nullable = false)
  private boolean consumed;

  protected PetitionSignatureVerificationJpaEntity() {}

  public PetitionSignatureVerificationJpaEntity(
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
    this.id = id;
    this.petitionId = petitionId;
    this.citizenAccountId = citizenAccountId;
    this.code = code;
    this.contact = contact;
    this.method = method;
    this.fullName = fullName;
    this.cpf = cpf;
    this.birthDate = birthDate;
    this.city = city;
    this.state = state;
    this.electoralData = electoralData;
    this.eSignatureConsent = eSignatureConsent;
    this.typedSignature = typedSignature;
    this.expiresAt = expiresAt;
    this.consumed = consumed;
  }

  public UUID getId() {
    return id;
  }

  public UUID getPetitionId() {
    return petitionId;
  }

  public UUID getCitizenAccountId() {
    return citizenAccountId;
  }

  public String getCode() {
    return code;
  }

  public String getContact() {
    return contact;
  }

  public String getMethod() {
    return method;
  }

  public String getFullName() {
    return fullName;
  }

  public String getCpf() {
    return cpf;
  }

  public LocalDate getBirthDate() {
    return birthDate;
  }

  public String getCity() {
    return city;
  }

  public String getState() {
    return state;
  }

  public String getElectoralData() {
    return electoralData;
  }

  public boolean isESignatureConsent() {
    return eSignatureConsent;
  }

  public String getTypedSignature() {
    return typedSignature;
  }

  public Instant getExpiresAt() {
    return expiresAt;
  }

  public boolean isConsumed() {
    return consumed;
  }
}
