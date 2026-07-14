package dev.civicpulse.directory.domain.model;

/** Mirrors the {@code gov_level_options} parameter table. */
public enum GovLevel {
  FEDERAL("federal"),
  STATE("state"),
  MUNICIPAL("municipal");

  private final String code;

  GovLevel(String code) {
    this.code = code;
  }

  public String code() {
    return code;
  }

  public static GovLevel fromCode(String code) {
    for (GovLevel level : values()) {
      if (level.code.equals(code)) {
        return level;
      }
    }
    throw new IllegalArgumentException("Unknown gov_level code: " + code);
  }
}
