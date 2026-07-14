package dev.civicpulse.participation.adapter.out.persistence;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

public class ConsultationResponseId implements Serializable {

  private UUID consultationId;
  private UUID citizenAccountId;

  protected ConsultationResponseId() {}

  public ConsultationResponseId(UUID consultationId, UUID citizenAccountId) {
    this.consultationId = consultationId;
    this.citizenAccountId = citizenAccountId;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof ConsultationResponseId other)) return false;
    return Objects.equals(consultationId, other.consultationId) && Objects.equals(citizenAccountId, other.citizenAccountId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(consultationId, citizenAccountId);
  }
}
