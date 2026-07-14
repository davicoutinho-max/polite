package dev.civicpulse.participation.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "surveys")
public class SurveyJpaEntity {

  @Id private UUID id;

  @Column(nullable = false)
  private String question;

  private String context;

  protected SurveyJpaEntity() {}

  public SurveyJpaEntity(UUID id, String question, String context) {
    this.id = id;
    this.question = question;
    this.context = context;
  }

  public UUID getId() {
    return id;
  }

  public String getQuestion() {
    return question;
  }

  public String getContext() {
    return context;
  }
}
