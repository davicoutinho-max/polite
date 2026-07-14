package dev.civicpulse.legislative.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "career_milestones")
public class CareerMilestoneJpaEntity {

  @Id
  @GeneratedValue
  private UUID id;

  @Column(name = "politician_account_id", nullable = false)
  private UUID politicianAccountId;

  @Column(nullable = false)
  private short year;

  @Column(nullable = false)
  private String title;

  private String detail;

  protected CareerMilestoneJpaEntity() {}

  public CareerMilestoneJpaEntity(UUID id, UUID politicianAccountId, short year, String title, String detail) {
    this.id = id;
    this.politicianAccountId = politicianAccountId;
    this.year = year;
    this.title = title;
    this.detail = detail;
  }

  public UUID getId() {
    return id;
  }

  public UUID getPoliticianAccountId() {
    return politicianAccountId;
  }

  public short getYear() {
    return year;
  }

  public String getTitle() {
    return title;
  }

  public String getDetail() {
    return detail;
  }
}
