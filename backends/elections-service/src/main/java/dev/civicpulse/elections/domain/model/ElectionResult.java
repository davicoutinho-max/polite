package dev.civicpulse.elections.domain.model;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/** One candidate's real tally in one race (office) of an election — unlike {@link
 * ElectionCandidacy}, which only links an already-onboarded politician account, this stores the
 * candidate's name/party/vote-count directly, since most candidates in a race (everyone who
 * didn't win) never get a platform account at all. {@code rank} is 1-indexed within
 * {@code (electionId, office)} — "who came in 2nd" is rank 2 in that group, not a global
 * ranking across every office an Election happens to bundle (e.g. Governador and Deputado
 * Estadual share one Election but are two separate races). */
public final class ElectionResult {

  private final UUID id;
  private final UUID electionId;
  private final String office;
  private final String externalId;
  private final String candidateName;
  private final String partyAcronym;
  private final long votes;
  private final int rank;
  private final boolean elected;
  private final UUID politicianAccountId;

  private ElectionResult(
      UUID id,
      UUID electionId,
      String office,
      String externalId,
      String candidateName,
      String partyAcronym,
      long votes,
      int rank,
      boolean elected,
      UUID politicianAccountId) {
    this.id = Objects.requireNonNull(id);
    this.electionId = Objects.requireNonNull(electionId);
    this.office = requireNonBlank(office);
    this.externalId = requireNonBlank(externalId);
    this.candidateName = requireNonBlank(candidateName);
    this.partyAcronym = partyAcronym;
    this.votes = votes;
    this.rank = rank;
    this.elected = elected;
    this.politicianAccountId = politicianAccountId;
  }

  public static ElectionResult create(
      UUID id,
      UUID electionId,
      String office,
      String externalId,
      String candidateName,
      String partyAcronym,
      long votes,
      int rank,
      boolean elected,
      UUID politicianAccountId) {
    return new ElectionResult(id, electionId, office, externalId, candidateName, partyAcronym, votes, rank, elected, politicianAccountId);
  }

  public static ElectionResult reconstitute(
      UUID id,
      UUID electionId,
      String office,
      String externalId,
      String candidateName,
      String partyAcronym,
      long votes,
      int rank,
      boolean elected,
      UUID politicianAccountId) {
    return new ElectionResult(id, electionId, office, externalId, candidateName, partyAcronym, votes, rank, elected, politicianAccountId);
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

  public UUID electionId() {
    return electionId;
  }

  public String office() {
    return office;
  }

  public String externalId() {
    return externalId;
  }

  public String candidateName() {
    return candidateName;
  }

  public Optional<String> partyAcronym() {
    return Optional.ofNullable(partyAcronym);
  }

  public long votes() {
    return votes;
  }

  public int rank() {
    return rank;
  }

  public boolean elected() {
    return elected;
  }

  public Optional<UUID> politicianAccountId() {
    return Optional.ofNullable(politicianAccountId);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof ElectionResult other)) return false;
    return id.equals(other.id);
  }

  @Override
  public int hashCode() {
    return id.hashCode();
  }
}
