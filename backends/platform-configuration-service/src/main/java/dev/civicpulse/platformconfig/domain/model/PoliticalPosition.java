package dev.civicpulse.platformconfig.domain.model;

import java.util.Objects;
import java.util.UUID;

/** A parametrized elected office (cargo) — e.g. "Vereador", "Deputado Federal" — offered when
 * registering a politician. Kept as data, not an enum, so platform admins can add any position
 * used across Brazilian politics without a code change. */
public final class PoliticalPosition {

  private final UUID id;
  private final String name;
  private final int sortOrder;

  private PoliticalPosition(UUID id, String name, int sortOrder) {
    this.id = Objects.requireNonNull(id);
    if (name == null || name.isBlank()) {
      throw new IllegalArgumentException("name must not be blank");
    }
    this.name = name;
    this.sortOrder = sortOrder;
  }

  public static PoliticalPosition create(UUID id, String name, int sortOrder) {
    return new PoliticalPosition(id, name, sortOrder);
  }

  public static PoliticalPosition reconstitute(UUID id, String name, int sortOrder) {
    return new PoliticalPosition(id, name, sortOrder);
  }

  public UUID id() {
    return id;
  }

  public String name() {
    return name;
  }

  public int sortOrder() {
    return sortOrder;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof PoliticalPosition other)) return false;
    return id.equals(other.id);
  }

  @Override
  public int hashCode() {
    return id.hashCode();
  }
}
