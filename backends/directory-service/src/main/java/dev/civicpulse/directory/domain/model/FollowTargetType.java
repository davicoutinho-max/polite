package dev.civicpulse.directory.domain.model;

/** Mirrors the {@code follow_target_type_options} parameter table. */
public enum FollowTargetType {
  POLITICIAN("politician"),
  PARTY("party");

  private final String code;

  FollowTargetType(String code) {
    this.code = code;
  }

  public String code() {
    return code;
  }

  public static FollowTargetType fromCode(String code) {
    for (FollowTargetType type : values()) {
      if (type.code.equals(code)) {
        return type;
      }
    }
    throw new IllegalArgumentException("Unknown follow_target_type code: " + code);
  }
}
