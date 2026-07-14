package dev.civicpulse.legislative.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/** Extends directory-service's lean politician projection with the deep-dossier fields it was
 * never meant to own (see schema.sql's header). No framework imports — the domain core of the
 * hexagonal architecture (see docs/architecture/system-architecture.html). */
public final class PoliticianDossierExtension {

  private final UUID politicianAccountId;
  private String education;
  private String profession;
  private String patrimony;
  private String email;
  private String phone;
  private String officeDetail;
  private int speechesCount;
  private int interviewsCount;
  private int tripsCount;
  private final Instant createdAt;

  private PoliticianDossierExtension(
      UUID politicianAccountId,
      String education,
      String profession,
      String patrimony,
      String email,
      String phone,
      String officeDetail,
      int speechesCount,
      int interviewsCount,
      int tripsCount,
      Instant createdAt) {
    this.politicianAccountId = Objects.requireNonNull(politicianAccountId);
    this.education = education;
    this.profession = profession;
    this.patrimony = patrimony;
    this.email = email;
    this.phone = phone;
    this.officeDetail = officeDetail;
    this.speechesCount = speechesCount;
    this.interviewsCount = interviewsCount;
    this.tripsCount = tripsCount;
    this.createdAt = Objects.requireNonNull(createdAt);
  }

  public static PoliticianDossierExtension createStub(UUID politicianAccountId, Instant now) {
    return new PoliticianDossierExtension(politicianAccountId, null, null, null, null, null, null, 0, 0, 0, now);
  }

  public static PoliticianDossierExtension reconstitute(
      UUID politicianAccountId,
      String education,
      String profession,
      String patrimony,
      String email,
      String phone,
      String officeDetail,
      int speechesCount,
      int interviewsCount,
      int tripsCount,
      Instant createdAt) {
    return new PoliticianDossierExtension(
        politicianAccountId, education, profession, patrimony, email, phone, officeDetail, speechesCount, interviewsCount, tripsCount, createdAt);
  }

  public void updateDossier(String education, String profession, String patrimony, String email, String phone, String officeDetail) {
    this.education = education;
    this.profession = profession;
    this.patrimony = patrimony;
    this.email = email;
    this.phone = phone;
    this.officeDetail = officeDetail;
  }

  public void updateActivityCounts(int speechesCount, int interviewsCount, int tripsCount) {
    this.speechesCount = speechesCount;
    this.interviewsCount = interviewsCount;
    this.tripsCount = tripsCount;
  }

  public UUID politicianAccountId() {
    return politicianAccountId;
  }

  public Optional<String> education() {
    return Optional.ofNullable(education);
  }

  public Optional<String> profession() {
    return Optional.ofNullable(profession);
  }

  public Optional<String> patrimony() {
    return Optional.ofNullable(patrimony);
  }

  public Optional<String> email() {
    return Optional.ofNullable(email);
  }

  public Optional<String> phone() {
    return Optional.ofNullable(phone);
  }

  public Optional<String> officeDetail() {
    return Optional.ofNullable(officeDetail);
  }

  public int speechesCount() {
    return speechesCount;
  }

  public int interviewsCount() {
    return interviewsCount;
  }

  public int tripsCount() {
    return tripsCount;
  }

  public Instant createdAt() {
    return createdAt;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof PoliticianDossierExtension other)) return false;
    return politicianAccountId.equals(other.politicianAccountId);
  }

  @Override
  public int hashCode() {
    return politicianAccountId.hashCode();
  }
}
