package dev.civicpulse.participation.adapter.out.persistence;

import dev.civicpulse.participation.domain.model.ConsultationStance;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "consultation_responses")
@IdClass(ConsultationResponseId.class)
public class ConsultationResponseJpaEntity {

  @Id
  @Column(name = "consultation_id")
  private UUID consultationId;

  @Id
  @Column(name = "citizen_account_id")
  private UUID citizenAccountId;

  @Column(nullable = false)
  private ConsultationStance stance;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  protected ConsultationResponseJpaEntity() {}

  public ConsultationResponseJpaEntity(UUID consultationId, UUID citizenAccountId, ConsultationStance stance, Instant updatedAt) {
    this.consultationId = consultationId;
    this.citizenAccountId = citizenAccountId;
    this.stance = stance;
    this.updatedAt = updatedAt;
  }

  public UUID getConsultationId() {
    return consultationId;
  }

  public UUID getCitizenAccountId() {
    return citizenAccountId;
  }

  public ConsultationStance getStance() {
    return stance;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }
}
