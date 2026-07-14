package dev.civicpulse.partymanagement.domain.model;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/** A regional directory (Nacional/Estadual/Municipal) for a party, each with its own leader. */
public final class PartyOffice {

  private final UUID id;
  private final UUID partyId;
  private final PartyOfficeScope scope;
  private final String location;
  private String leaderName;
  private int memberCount;

  private PartyOffice(UUID id, UUID partyId, PartyOfficeScope scope, String location, String leaderName, int memberCount) {
    this.id = Objects.requireNonNull(id);
    this.partyId = Objects.requireNonNull(partyId);
    this.scope = Objects.requireNonNull(scope);
    this.location = requireNonBlank(location, "location");
    this.leaderName = leaderName;
    this.memberCount = memberCount;
  }

  public static PartyOffice create(UUID id, UUID partyId, PartyOfficeScope scope, String location, String leaderName) {
    return new PartyOffice(id, partyId, scope, location, leaderName, 0);
  }

  public static PartyOffice reconstitute(
      UUID id, UUID partyId, PartyOfficeScope scope, String location, String leaderName, int memberCount) {
    return new PartyOffice(id, partyId, scope, location, leaderName, memberCount);
  }

  public void assignLeader(String leaderName) {
    this.leaderName = leaderName;
  }

  private static String requireNonBlank(String value, String field) {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException(field + " must not be blank");
    }
    return value;
  }

  public UUID id() {
    return id;
  }

  public UUID partyId() {
    return partyId;
  }

  public PartyOfficeScope scope() {
    return scope;
  }

  public String location() {
    return location;
  }

  public Optional<String> leaderName() {
    return Optional.ofNullable(leaderName);
  }

  public int memberCount() {
    return memberCount;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof PartyOffice other)) return false;
    return id.equals(other.id);
  }

  @Override
  public int hashCode() {
    return id.hashCode();
  }
}
