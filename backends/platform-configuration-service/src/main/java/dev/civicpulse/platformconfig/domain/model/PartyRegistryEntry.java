package dev.civicpulse.platformconfig.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/** System-of-record for a party's legal identity (acronym/number are electoral-court-issued).
 * Directory Service holds the public-facing read shadow, projected from {@code
 * PartyRegistered}. */
public final class PartyRegistryEntry {

  private final UUID id;
  private final String name;
  private final String acronym;
  private final int number;
  private String president;
  private String ideology;
  private int memberCount;
  private final Instant createdAt;

  private PartyRegistryEntry(
      UUID id, String name, String acronym, int number, String president, String ideology, int memberCount, Instant createdAt) {
    this.id = Objects.requireNonNull(id);
    this.name = requireNonBlank(name, "name");
    this.acronym = requireNonBlank(acronym, "acronym");
    this.number = number;
    this.president = president;
    this.ideology = ideology;
    this.memberCount = memberCount;
    this.createdAt = Objects.requireNonNull(createdAt);
  }

  public static PartyRegistryEntry register(UUID id, String name, String acronym, int number, String president, String ideology, Instant now) {
    return new PartyRegistryEntry(id, name, acronym, number, president, ideology, 0, now);
  }

  public static PartyRegistryEntry reconstitute(
      UUID id, String name, String acronym, int number, String president, String ideology, int memberCount, Instant createdAt) {
    return new PartyRegistryEntry(id, name, acronym, number, president, ideology, memberCount, createdAt);
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

  public String name() {
    return name;
  }

  public String acronym() {
    return acronym;
  }

  public int number() {
    return number;
  }

  public Optional<String> president() {
    return Optional.ofNullable(president);
  }

  public Optional<String> ideology() {
    return Optional.ofNullable(ideology);
  }

  public int memberCount() {
    return memberCount;
  }

  public Instant createdAt() {
    return createdAt;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof PartyRegistryEntry other)) return false;
    return id.equals(other.id);
  }

  @Override
  public int hashCode() {
    return id.hashCode();
  }
}
