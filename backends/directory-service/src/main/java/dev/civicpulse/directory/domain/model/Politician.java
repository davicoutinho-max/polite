package dev.civicpulse.directory.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Eventually-consistent read projection of a politician, built from events published by
 * Identity and Party Management (see docs/architecture/data-architecture.html's Event Flow
 * Map). Never written to directly by end users — only ever mutated by consuming
 * {@code AccountRegistered}, {@code PoliticianRegistered}, {@code PoliticianReassigned},
 * {@code AccountVerified}, and by this service's own follow/unfollow use case (which owns
 * {@code followers_count} locally rather than round-tripping through Kafka).
 *
 * <p>No framework imports — the domain core of the hexagonal architecture documented in
 * docs/architecture/system-architecture.html.
 */
public final class Politician {

  private final UUID accountId;
  private String name;
  private String handle;
  private String avatarUrl;
  private String coverImageUrl;
  private boolean verified;
  private String office;
  private GovLevel level;
  private UUID partyId;
  private String partyAcronym;
  private String state;
  private int followersCount;
  private int billsCount;
  private Instant updatedAt;

  private Politician(
      UUID accountId,
      String name,
      String handle,
      String avatarUrl,
      String coverImageUrl,
      boolean verified,
      String office,
      GovLevel level,
      UUID partyId,
      String partyAcronym,
      String state,
      int followersCount,
      int billsCount,
      Instant updatedAt) {
    this.accountId = Objects.requireNonNull(accountId);
    this.name = requireNonBlank(name, "name");
    this.handle = requireNonBlank(handle, "handle");
    this.avatarUrl = avatarUrl;
    this.coverImageUrl = coverImageUrl;
    this.verified = verified;
    this.office = office;
    this.level = level;
    this.partyId = partyId;
    this.partyAcronym = partyAcronym;
    this.state = state;
    this.followersCount = followersCount;
    this.billsCount = billsCount;
    this.updatedAt = Objects.requireNonNull(updatedAt);
  }

  /** Projects a brand-new politician row — triggered by {@code PoliticianRegistered} (or the
   * initial {@code RepresentativeLinked}, whichever this service consumes first). */
  public static Politician project(
      UUID accountId,
      String name,
      String handle,
      String avatarUrl,
      String office,
      GovLevel level,
      UUID partyId,
      String partyAcronym,
      String state,
      Instant now) {
    return new Politician(accountId, name, handle, avatarUrl, null, false, office, level, partyId, partyAcronym, state, 0, 0, now);
  }

  public static Politician reconstitute(
      UUID accountId,
      String name,
      String handle,
      String avatarUrl,
      String coverImageUrl,
      boolean verified,
      String office,
      GovLevel level,
      UUID partyId,
      String partyAcronym,
      String state,
      int followersCount,
      int billsCount,
      Instant updatedAt) {
    return new Politician(
        accountId, name, handle, avatarUrl, coverImageUrl, verified, office, level, partyId, partyAcronym, state, followersCount,
        billsCount, updatedAt);
  }

  /** Self-service update — a politician changing their own avatar/cover photo. Either argument
   * left {@code null} leaves that field untouched (lets the frontend update just one of the two
   * without having to resend the other). */
  public void updateProfileImages(String avatarUrl, String coverImageUrl, Instant now) {
    if (avatarUrl != null) {
      this.avatarUrl = avatarUrl;
    }
    if (coverImageUrl != null) {
      this.coverImageUrl = coverImageUrl;
    }
    this.updatedAt = now;
  }

  /** Applies a {@code PoliticianReassigned} event — the only path that changes party/office/
   * level/state after initial projection. */
  public void reassign(String office, GovLevel level, UUID partyId, String partyAcronym, String state, Instant now) {
    this.office = office;
    this.level = level;
    this.partyId = partyId;
    this.partyAcronym = partyAcronym;
    this.state = state;
    this.updatedAt = now;
  }

  public void incrementFollowers(Instant now) {
    this.followersCount++;
    this.updatedAt = now;
  }

  public void decrementFollowers(Instant now) {
    this.followersCount = Math.max(0, this.followersCount - 1);
    this.updatedAt = now;
  }

  private static String requireNonBlank(String value, String field) {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException(field + " must not be blank");
    }
    return value;
  }

  public UUID accountId() {
    return accountId;
  }

  public String name() {
    return name;
  }

  public String handle() {
    return handle;
  }

  public Optional<String> avatarUrl() {
    return Optional.ofNullable(avatarUrl);
  }

  public Optional<String> coverImageUrl() {
    return Optional.ofNullable(coverImageUrl);
  }

  public boolean verified() {
    return verified;
  }

  public Optional<String> office() {
    return Optional.ofNullable(office);
  }

  public Optional<GovLevel> level() {
    return Optional.ofNullable(level);
  }

  public Optional<UUID> partyId() {
    return Optional.ofNullable(partyId);
  }

  public Optional<String> partyAcronym() {
    return Optional.ofNullable(partyAcronym);
  }

  public Optional<String> state() {
    return Optional.ofNullable(state);
  }

  public int followersCount() {
    return followersCount;
  }

  public int billsCount() {
    return billsCount;
  }

  public Instant updatedAt() {
    return updatedAt;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Politician other)) return false;
    return accountId.equals(other.accountId);
  }

  @Override
  public int hashCode() {
    return accountId.hashCode();
  }
}
