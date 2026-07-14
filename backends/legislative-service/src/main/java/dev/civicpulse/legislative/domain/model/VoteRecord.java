package dev.civicpulse.legislative.domain.model;

import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/** {@code legislativeItemId} is nullable — not every recorded vote corresponds to a formally
 * tracked {@link LegislativeItem} (e.g. procedural votes); {@code matter} is always a free-text
 * label either way, exactly like the original mock's VoteRecord.matter. */
public final class VoteRecord {

  private final UUID id;
  private final UUID politicianAccountId;
  private final UUID legislativeItemId;
  private final String matter;
  private final LocalDate voteDate;
  private final VoteChoice choice;

  private VoteRecord(UUID id, UUID politicianAccountId, UUID legislativeItemId, String matter, LocalDate voteDate, VoteChoice choice) {
    this.id = id;
    this.politicianAccountId = Objects.requireNonNull(politicianAccountId);
    this.legislativeItemId = legislativeItemId;
    this.matter = requireNonBlank(matter, "matter");
    this.voteDate = Objects.requireNonNull(voteDate);
    this.choice = Objects.requireNonNull(choice);
  }

  public static VoteRecord cast(UUID politicianAccountId, UUID legislativeItemId, String matter, LocalDate voteDate, VoteChoice choice) {
    return new VoteRecord(null, politicianAccountId, legislativeItemId, matter, voteDate, choice);
  }

  public static VoteRecord reconstitute(
      UUID id, UUID politicianAccountId, UUID legislativeItemId, String matter, LocalDate voteDate, VoteChoice choice) {
    return new VoteRecord(id, politicianAccountId, legislativeItemId, matter, voteDate, choice);
  }

  private static String requireNonBlank(String value, String field) {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException(field + " must not be blank");
    }
    return value;
  }

  public Optional<UUID> id() {
    return Optional.ofNullable(id);
  }

  public UUID politicianAccountId() {
    return politicianAccountId;
  }

  public Optional<UUID> legislativeItemId() {
    return Optional.ofNullable(legislativeItemId);
  }

  public String matter() {
    return matter;
  }

  public LocalDate voteDate() {
    return voteDate;
  }

  public VoteChoice choice() {
    return choice;
  }
}
