package dev.civicpulse.fundraising.domain.model;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/** The fundraiser aggregate — {@code raisedCents}/{@code supportersCount} are denormalized
 * counters, mutated only through {@link #recordContribution(long)} so they can never drift from
 * the underlying {@code contributions} rows (see FundraisingServiceApplication's note on how
 * contributions are only ever written after a real captured payment). No framework imports — the
 * domain core of the hexagonal architecture (see docs/architecture/system-architecture.html). */
public final class Fundraiser {

  private final UUID id;
  private final UUID organizerAccountId;
  private final String title;
  private final String description;
  private final FundraiserCategory category;
  private final long goalCents;
  private long raisedCents;
  private int supportersCount;
  private final LocalDate deadline;
  private final boolean ledgerPublic;
  private final Instant createdAt;

  private Fundraiser(
      UUID id,
      UUID organizerAccountId,
      String title,
      String description,
      FundraiserCategory category,
      long goalCents,
      long raisedCents,
      int supportersCount,
      LocalDate deadline,
      boolean ledgerPublic,
      Instant createdAt) {
    this.id = Objects.requireNonNull(id);
    this.organizerAccountId = Objects.requireNonNull(organizerAccountId);
    this.title = requireNonBlank(title, "title");
    this.description = description;
    this.category = Objects.requireNonNull(category);
    if (goalCents <= 0) {
      throw new IllegalArgumentException("goalCents must be positive");
    }
    this.goalCents = goalCents;
    this.raisedCents = raisedCents;
    this.supportersCount = supportersCount;
    this.deadline = deadline;
    this.ledgerPublic = ledgerPublic;
    this.createdAt = Objects.requireNonNull(createdAt);
  }

  public static Fundraiser create(
      UUID id,
      UUID organizerAccountId,
      String title,
      String description,
      FundraiserCategory category,
      long goalCents,
      LocalDate deadline,
      boolean ledgerPublic,
      Instant now) {
    return new Fundraiser(id, organizerAccountId, title, description, category, goalCents, 0, 0, deadline, ledgerPublic, now);
  }

  public static Fundraiser reconstitute(
      UUID id,
      UUID organizerAccountId,
      String title,
      String description,
      FundraiserCategory category,
      long goalCents,
      long raisedCents,
      int supportersCount,
      LocalDate deadline,
      boolean ledgerPublic,
      Instant createdAt) {
    return new Fundraiser(
        id, organizerAccountId, title, description, category, goalCents, raisedCents, supportersCount, deadline, ledgerPublic, createdAt);
  }

  /** Returns {@code true} exactly once — the call that pushes {@code raisedCents} from below the
   * goal to at/above it — so the caller knows precisely when to publish
   * {@code FundraiserGoalReached}. */
  public boolean recordContribution(long amountCents) {
    if (amountCents <= 0) {
      throw new IllegalArgumentException("amountCents must be positive");
    }
    boolean wasBelowGoal = raisedCents < goalCents;
    raisedCents += amountCents;
    supportersCount++;
    return wasBelowGoal && raisedCents >= goalCents;
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

  public UUID organizerAccountId() {
    return organizerAccountId;
  }

  public String title() {
    return title;
  }

  public Optional<String> description() {
    return Optional.ofNullable(description);
  }

  public FundraiserCategory category() {
    return category;
  }

  public long goalCents() {
    return goalCents;
  }

  public long raisedCents() {
    return raisedCents;
  }

  public int supportersCount() {
    return supportersCount;
  }

  public Optional<LocalDate> deadline() {
    return Optional.ofNullable(deadline);
  }

  public boolean ledgerPublic() {
    return ledgerPublic;
  }

  public Instant createdAt() {
    return createdAt;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Fundraiser other)) return false;
    return id.equals(other.id);
  }

  @Override
  public int hashCode() {
    return id.hashCode();
  }
}
