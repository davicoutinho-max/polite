package dev.civicpulse.partymanagement.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public final class PartyMember {

  private final UUID id;
  private final UUID partyId;
  private final UUID citizenAccountId;
  private final String city;
  private PartyMemberStatus status;
  private final Instant joinedAt;

  private PartyMember(UUID id, UUID partyId, UUID citizenAccountId, String city, PartyMemberStatus status, Instant joinedAt) {
    this.id = Objects.requireNonNull(id);
    this.partyId = Objects.requireNonNull(partyId);
    this.citizenAccountId = Objects.requireNonNull(citizenAccountId);
    this.city = city;
    this.status = Objects.requireNonNull(status);
    this.joinedAt = Objects.requireNonNull(joinedAt);
  }

  public static PartyMember admit(UUID id, UUID partyId, UUID citizenAccountId, String city, Instant now) {
    return new PartyMember(id, partyId, citizenAccountId, city, PartyMemberStatus.ACTIVE, now);
  }

  public static PartyMember reconstitute(
      UUID id, UUID partyId, UUID citizenAccountId, String city, PartyMemberStatus status, Instant joinedAt) {
    return new PartyMember(id, partyId, citizenAccountId, city, status, joinedAt);
  }

  public void changeStatus(PartyMemberStatus newStatus) {
    this.status = Objects.requireNonNull(newStatus);
  }

  public UUID id() {
    return id;
  }

  public UUID partyId() {
    return partyId;
  }

  public UUID citizenAccountId() {
    return citizenAccountId;
  }

  public Optional<String> city() {
    return Optional.ofNullable(city);
  }

  public PartyMemberStatus status() {
    return status;
  }

  public Instant joinedAt() {
    return joinedAt;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof PartyMember other)) return false;
    return id.equals(other.id);
  }

  @Override
  public int hashCode() {
    return id.hashCode();
  }
}
