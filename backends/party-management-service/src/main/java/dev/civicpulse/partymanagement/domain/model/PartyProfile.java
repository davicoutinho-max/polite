package dev.civicpulse.partymanagement.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/** The party's own editable "about" content — separate from the legal registry row owned by
 * Platform Configuration (see docs/architecture/data-architecture.html). Created blank the
 * moment {@code PartyRegistered} is consumed, then edited by the party's own admins. */
public final class PartyProfile {

  private final UUID partyId;
  private String history;
  private String program;
  private String statuteUrl;
  private String coverUrl;
  private Instant updatedAt;

  private PartyProfile(UUID partyId, String history, String program, String statuteUrl, String coverUrl, Instant updatedAt) {
    this.partyId = Objects.requireNonNull(partyId);
    this.history = history;
    this.program = program;
    this.statuteUrl = statuteUrl;
    this.coverUrl = coverUrl;
    this.updatedAt = Objects.requireNonNull(updatedAt);
  }

  public static PartyProfile createBlank(UUID partyId, Instant now) {
    return new PartyProfile(partyId, null, null, null, null, now);
  }

  public static PartyProfile reconstitute(
      UUID partyId, String history, String program, String statuteUrl, String coverUrl, Instant updatedAt) {
    return new PartyProfile(partyId, history, program, statuteUrl, coverUrl, updatedAt);
  }

  public void update(String history, String program, String statuteUrl, String coverUrl, Instant now) {
    this.history = history;
    this.program = program;
    this.statuteUrl = statuteUrl;
    this.coverUrl = coverUrl;
    this.updatedAt = now;
  }

  public UUID partyId() {
    return partyId;
  }

  public Optional<String> history() {
    return Optional.ofNullable(history);
  }

  public Optional<String> program() {
    return Optional.ofNullable(program);
  }

  public Optional<String> statuteUrl() {
    return Optional.ofNullable(statuteUrl);
  }

  public Optional<String> coverUrl() {
    return Optional.ofNullable(coverUrl);
  }

  public Instant updatedAt() {
    return updatedAt;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof PartyProfile other)) return false;
    return partyId.equals(other.partyId);
  }

  @Override
  public int hashCode() {
    return partyId.hashCode();
  }
}
