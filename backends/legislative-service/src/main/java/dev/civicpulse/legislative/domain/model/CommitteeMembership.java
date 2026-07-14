package dev.civicpulse.legislative.domain.model;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public final class CommitteeMembership {

  private final UUID id;
  private final UUID politicianAccountId;
  private final String name;
  private final String role;
  private final CommitteeKind kind;

  private CommitteeMembership(UUID id, UUID politicianAccountId, String name, String role, CommitteeKind kind) {
    this.id = id;
    this.politicianAccountId = Objects.requireNonNull(politicianAccountId);
    this.name = requireNonBlank(name, "name");
    this.role = requireNonBlank(role, "role");
    this.kind = Objects.requireNonNull(kind);
  }

  public static CommitteeMembership join(UUID politicianAccountId, String name, String role, CommitteeKind kind) {
    return new CommitteeMembership(null, politicianAccountId, name, role, kind);
  }

  public static CommitteeMembership reconstitute(UUID id, UUID politicianAccountId, String name, String role, CommitteeKind kind) {
    return new CommitteeMembership(id, politicianAccountId, name, role, kind);
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

  public String name() {
    return name;
  }

  public String role() {
    return role;
  }

  public CommitteeKind kind() {
    return kind;
  }
}
