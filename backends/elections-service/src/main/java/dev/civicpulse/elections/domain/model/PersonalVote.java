package dev.civicpulse.elections.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/** A citizen's own, personal record of who they picked for one office of an election — never the
 * official secret ballot, purely a self-reported memory aid (see schema.sql's header on this
 * table for the constitutional reasoning). */
public final class PersonalVote {

  private final UUID id;
  private final UUID citizenAccountId;
  private final UUID electionId;
  private final String office;
  private final String candidateName;
  private final String candidatePartyAcronym;
  private final UUID politicianAccountId;
  private final Instant castAt;

  private PersonalVote(
      UUID id,
      UUID citizenAccountId,
      UUID electionId,
      String office,
      String candidateName,
      String candidatePartyAcronym,
      UUID politicianAccountId,
      Instant castAt) {
    this.id = Objects.requireNonNull(id);
    this.citizenAccountId = Objects.requireNonNull(citizenAccountId);
    this.electionId = Objects.requireNonNull(electionId);
    this.office = requireNonBlank(office);
    this.candidateName = requireNonBlank(candidateName);
    this.candidatePartyAcronym = candidatePartyAcronym;
    this.politicianAccountId = politicianAccountId;
    this.castAt = Objects.requireNonNull(castAt);
  }

  public static PersonalVote cast(
      UUID id,
      UUID citizenAccountId,
      UUID electionId,
      String office,
      String candidateName,
      String candidatePartyAcronym,
      UUID politicianAccountId,
      Instant castAt) {
    return new PersonalVote(id, citizenAccountId, electionId, office, candidateName, candidatePartyAcronym, politicianAccountId, castAt);
  }

  public static PersonalVote reconstitute(
      UUID id,
      UUID citizenAccountId,
      UUID electionId,
      String office,
      String candidateName,
      String candidatePartyAcronym,
      UUID politicianAccountId,
      Instant castAt) {
    return new PersonalVote(id, citizenAccountId, electionId, office, candidateName, candidatePartyAcronym, politicianAccountId, castAt);
  }

  private static String requireNonBlank(String value) {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException("value must not be blank");
    }
    return value;
  }

  public UUID id() {
    return id;
  }

  public UUID citizenAccountId() {
    return citizenAccountId;
  }

  public UUID electionId() {
    return electionId;
  }

  public String office() {
    return office;
  }

  public String candidateName() {
    return candidateName;
  }

  public Optional<String> candidatePartyAcronym() {
    return Optional.ofNullable(candidatePartyAcronym);
  }

  public Optional<UUID> politicianAccountId() {
    return Optional.ofNullable(politicianAccountId);
  }

  public Instant castAt() {
    return castAt;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof PersonalVote other)) return false;
    return id.equals(other.id);
  }

  @Override
  public int hashCode() {
    return id.hashCode();
  }
}
