package dev.civicpulse.participation.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "consultations")
public class ConsultationJpaEntity {

  @Id private UUID id;

  @Column(nullable = false)
  private String title;

  private String description;

  private LocalDate deadline;

  @Column(name = "responses_count", nullable = false)
  private int responsesCount;

  protected ConsultationJpaEntity() {}

  public ConsultationJpaEntity(UUID id, String title, String description, LocalDate deadline, int responsesCount) {
    this.id = id;
    this.title = title;
    this.description = description;
    this.deadline = deadline;
    this.responsesCount = responsesCount;
  }

  public UUID getId() {
    return id;
  }

  public String getTitle() {
    return title;
  }

  public String getDescription() {
    return description;
  }

  public LocalDate getDeadline() {
    return deadline;
  }

  public int getResponsesCount() {
    return responsesCount;
  }
}
