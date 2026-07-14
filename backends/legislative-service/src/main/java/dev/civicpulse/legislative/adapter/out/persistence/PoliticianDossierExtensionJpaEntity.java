package dev.civicpulse.legislative.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.JdbcType;

@Entity
@Table(name = "politician_dossier_extensions")
public class PoliticianDossierExtensionJpaEntity {

  @Id
  @Column(name = "politician_account_id")
  private UUID politicianAccountId;

  private String education;
  private String profession;
  private String patrimony;

  @JdbcType(CitextJdbcType.class)
  private String email;

  private String phone;

  @Column(name = "office_detail")
  private String officeDetail;

  @Column(name = "speeches_count", nullable = false)
  private int speechesCount;

  @Column(name = "interviews_count", nullable = false)
  private int interviewsCount;

  @Column(name = "trips_count", nullable = false)
  private int tripsCount;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  protected PoliticianDossierExtensionJpaEntity() {}

  public PoliticianDossierExtensionJpaEntity(
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
    this.politicianAccountId = politicianAccountId;
    this.education = education;
    this.profession = profession;
    this.patrimony = patrimony;
    this.email = email;
    this.phone = phone;
    this.officeDetail = officeDetail;
    this.speechesCount = speechesCount;
    this.interviewsCount = interviewsCount;
    this.tripsCount = tripsCount;
    this.createdAt = createdAt;
  }

  public UUID getPoliticianAccountId() {
    return politicianAccountId;
  }

  public String getEducation() {
    return education;
  }

  public String getProfession() {
    return profession;
  }

  public String getPatrimony() {
    return patrimony;
  }

  public String getEmail() {
    return email;
  }

  public String getPhone() {
    return phone;
  }

  public String getOfficeDetail() {
    return officeDetail;
  }

  public int getSpeechesCount() {
    return speechesCount;
  }

  public int getInterviewsCount() {
    return interviewsCount;
  }

  public int getTripsCount() {
    return tripsCount;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }
}
