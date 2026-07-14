package dev.civicpulse.privacycompliance.adapter.out.persistence;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

public class ConsentRecordId implements Serializable {

  private UUID accountId;
  private String purpose;

  protected ConsentRecordId() {}

  public ConsentRecordId(UUID accountId, String purpose) {
    this.accountId = accountId;
    this.purpose = purpose;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof ConsentRecordId other)) return false;
    return Objects.equals(accountId, other.accountId) && Objects.equals(purpose, other.purpose);
  }

  @Override
  public int hashCode() {
    return Objects.hash(accountId, purpose);
  }
}
