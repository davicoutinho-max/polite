package dev.civicpulse.participation.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "petition_signatures")
@IdClass(PetitionSignatureId.class)
public class PetitionSignatureJpaEntity {

  @Id
  @Column(name = "petition_id")
  private UUID petitionId;

  @Id
  @Column(name = "citizen_account_id")
  private UUID citizenAccountId;

  @Column(name = "signed_at", nullable = false)
  private Instant signedAt;

  @Column(name = "full_name")
  private String fullName;

  private String cpf;

  @Column(name = "birth_date")
  private LocalDate birthDate;

  private String city;

  private String state;

  @Column(name = "verification_method")
  private String verificationMethod;

  @Column(name = "electoral_data")
  private String electoralData;

  @Column(name = "e_signature_consent", nullable = false)
  private boolean eSignatureConsent;

  @Column(name = "identity_validated", nullable = false)
  private boolean identityValidated;

  @Column(name = "typed_signature")
  private String typedSignature;

  protected PetitionSignatureJpaEntity() {}

  public PetitionSignatureJpaEntity(
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
    this.petitionId = petitionId;
    this.citizenAccountId = citizenAccountId;
    this.signedAt = signedAt;
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

  public UUID getPetitionId() {
    return petitionId;
  }

  public UUID getCitizenAccountId() {
    return citizenAccountId;
  }

  public Instant getSignedAt() {
    return signedAt;
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

  public String getVerificationMethod() {
    return verificationMethod;
  }

  public String getElectoralData() {
    return electoralData;
  }

  public boolean isESignatureConsent() {
    return eSignatureConsent;
  }

  public boolean isIdentityValidated() {
    return identityValidated;
  }

  public String getTypedSignature() {
    return typedSignature;
  }
}
