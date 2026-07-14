package dev.civicpulse.directory.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Public party catalog entry — a read-facing shadow of the legal registry owned by Platform
 * Configuration (see docs/architecture/data-architecture.html). Projected from
 * {@code PartyRegistered}; {@code member_count} is maintained locally by this service's follow
 * use case in the same way {@link Politician#followersCount()} is.
 */
public final class Party {

  private final UUID id;
  private String name;
  private String acronym;
  private int number;
  private String ideology;
  private PartySpectrum spectrum;
  private Integer foundedYear;
  private String president;
  private String logoUrl;
  private int memberCount;
  private Instant updatedAt;

  private Party(
      UUID id,
      String name,
      String acronym,
      int number,
      String ideology,
      PartySpectrum spectrum,
      Integer foundedYear,
      String president,
      String logoUrl,
      int memberCount,
      Instant updatedAt) {
    this.id = Objects.requireNonNull(id);
    this.name = requireNonBlank(name, "name");
    this.acronym = requireNonBlank(acronym, "acronym");
    this.number = number;
    this.ideology = ideology;
    this.spectrum = spectrum;
    this.foundedYear = foundedYear;
    this.president = president;
    this.logoUrl = logoUrl;
    this.memberCount = memberCount;
    this.updatedAt = Objects.requireNonNull(updatedAt);
  }

  public static Party project(
      UUID id,
      String name,
      String acronym,
      int number,
      String ideology,
      PartySpectrum spectrum,
      Integer foundedYear,
      String president,
      String logoUrl,
      Instant now) {
    return new Party(id, name, acronym, number, ideology, spectrum, foundedYear, president, logoUrl, 0, now);
  }

  public static Party reconstitute(
      UUID id,
      String name,
      String acronym,
      int number,
      String ideology,
      PartySpectrum spectrum,
      Integer foundedYear,
      String president,
      String logoUrl,
      int memberCount,
      Instant updatedAt) {
    return new Party(id, name, acronym, number, ideology, spectrum, foundedYear, president, logoUrl, memberCount, updatedAt);
  }

  public void incrementMembers(Instant now) {
    this.memberCount++;
    this.updatedAt = now;
  }

  public void decrementMembers(Instant now) {
    this.memberCount = Math.max(0, this.memberCount - 1);
    this.updatedAt = now;
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

  public Optional<String> ideology() {
    return Optional.ofNullable(ideology);
  }

  public Optional<PartySpectrum> spectrum() {
    return Optional.ofNullable(spectrum);
  }

  public Optional<Integer> foundedYear() {
    return Optional.ofNullable(foundedYear);
  }

  public Optional<String> president() {
    return Optional.ofNullable(president);
  }

  public Optional<String> logoUrl() {
    return Optional.ofNullable(logoUrl);
  }

  public int memberCount() {
    return memberCount;
  }

  public Instant updatedAt() {
    return updatedAt;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Party other)) return false;
    return id.equals(other.id);
  }

  @Override
  public int hashCode() {
    return id.hashCode();
  }
}
