package dev.civicpulse.legislative.domain.model;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/** {@code valueCents} is a real integer, not a preformatted display string like the original
 * frontend mock (e.g. "R$ 41.650") — formatting happens client-side. */
public final class TransparencyMetric {

  private final UUID id;
  private final UUID politicianAccountId;
  private final String icon;
  private final String label;
  private final long valueCents;
  private final String caption;
  private final String period;

  private TransparencyMetric(UUID id, UUID politicianAccountId, String icon, String label, long valueCents, String caption, String period) {
    this.id = id;
    this.politicianAccountId = Objects.requireNonNull(politicianAccountId);
    this.icon = icon;
    this.label = requireNonBlank(label, "label");
    this.valueCents = valueCents;
    this.caption = caption;
    this.period = requireNonBlank(period, "period");
  }

  public static TransparencyMetric add(UUID politicianAccountId, String icon, String label, long valueCents, String caption, String period) {
    return new TransparencyMetric(null, politicianAccountId, icon, label, valueCents, caption, period);
  }

  public static TransparencyMetric reconstitute(
      UUID id, UUID politicianAccountId, String icon, String label, long valueCents, String caption, String period) {
    return new TransparencyMetric(id, politicianAccountId, icon, label, valueCents, caption, period);
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

  public Optional<String> icon() {
    return Optional.ofNullable(icon);
  }

  public String label() {
    return label;
  }

  public long valueCents() {
    return valueCents;
  }

  public Optional<String> caption() {
    return Optional.ofNullable(caption);
  }

  public String period() {
    return period;
  }
}
