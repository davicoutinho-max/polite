package dev.civicpulse.membershipaffiliation.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public final class MembershipCard {

  private final UUID affiliationId;
  private final String memberNumber;
  private final String qrPayload;
  private final Instant issuedAt;

  private MembershipCard(UUID affiliationId, String memberNumber, String qrPayload, Instant issuedAt) {
    this.affiliationId = Objects.requireNonNull(affiliationId);
    this.memberNumber = requireNonBlank(memberNumber, "memberNumber");
    this.qrPayload = requireNonBlank(qrPayload, "qrPayload");
    this.issuedAt = Objects.requireNonNull(issuedAt);
  }

  public static MembershipCard issue(UUID affiliationId, String memberNumber, String qrPayload, Instant now) {
    return new MembershipCard(affiliationId, memberNumber, qrPayload, now);
  }

  public static MembershipCard reconstitute(UUID affiliationId, String memberNumber, String qrPayload, Instant issuedAt) {
    return new MembershipCard(affiliationId, memberNumber, qrPayload, issuedAt);
  }

  private static String requireNonBlank(String value, String field) {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException(field + " must not be blank");
    }
    return value;
  }

  public UUID affiliationId() {
    return affiliationId;
  }

  public String memberNumber() {
    return memberNumber;
  }

  public String qrPayload() {
    return qrPayload;
  }

  public Instant issuedAt() {
    return issuedAt;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof MembershipCard other)) return false;
    return affiliationId.equals(other.affiliationId);
  }

  @Override
  public int hashCode() {
    return affiliationId.hashCode();
  }
}
