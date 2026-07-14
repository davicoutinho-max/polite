package dev.civicpulse.participation.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/** A citizen's current stance on a consultation — mutable in place (see
 * {@link Consultation#recordNewResponse()}). */
public final class ConsultationResponse {

  private final UUID consultationId;
  private final UUID citizenAccountId;
  private ConsultationStance stance;
  private Instant updatedAt;

  private ConsultationResponse(UUID consultationId, UUID citizenAccountId, ConsultationStance stance, Instant updatedAt) {
    this.consultationId = Objects.requireNonNull(consultationId);
    this.citizenAccountId = Objects.requireNonNull(citizenAccountId);
    this.stance = Objects.requireNonNull(stance);
    this.updatedAt = Objects.requireNonNull(updatedAt);
  }

  public static ConsultationResponse respond(UUID consultationId, UUID citizenAccountId, ConsultationStance stance, Instant now) {
    return new ConsultationResponse(consultationId, citizenAccountId, stance, now);
  }

  public static ConsultationResponse reconstitute(UUID consultationId, UUID citizenAccountId, ConsultationStance stance, Instant updatedAt) {
    return new ConsultationResponse(consultationId, citizenAccountId, stance, updatedAt);
  }

  public void changeStance(ConsultationStance stance, Instant now) {
    this.stance = Objects.requireNonNull(stance);
    this.updatedAt = now;
  }

  public UUID consultationId() {
    return consultationId;
  }

  public UUID citizenAccountId() {
    return citizenAccountId;
  }

  public ConsultationStance stance() {
    return stance;
  }

  public Instant updatedAt() {
    return updatedAt;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof ConsultationResponse other)) return false;
    return consultationId.equals(other.consultationId) && citizenAccountId.equals(other.citizenAccountId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(consultationId, citizenAccountId);
  }
}
