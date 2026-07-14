package dev.civicpulse.analytics.adapter.out.persistence;

/** JPQL constructor-expression projection for the by-content-type / by-account-type breakdowns.
 * {@code key} may be null when a dimension value could not be resolved at ingest time (e.g. an
 * identity-service lookup failure) — reported as-is rather than fabricated. */
public class TypeCountProjection {

  private final String key;
  private final long count;

  public TypeCountProjection(String key, long count) {
    this.key = key;
    this.count = count;
  }

  public String getKey() {
    return key;
  }

  public long getCount() {
    return count;
  }
}
