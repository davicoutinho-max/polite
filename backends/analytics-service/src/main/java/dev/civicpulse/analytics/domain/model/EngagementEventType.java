package dev.civicpulse.analytics.domain.model;

/** Mirrors {@code engagement_event_type_options}. */
public enum EngagementEventType {
  POST_PUBLISHED("post_published"),
  LIKE("like"),
  COMMENT("comment"),
  FOLLOW_CREATED("follow_created"),
  FOLLOW_REMOVED("follow_removed");

  private final String code;

  EngagementEventType(String code) {
    this.code = code;
  }

  public String code() {
    return code;
  }

  public static EngagementEventType fromCode(String code) {
    for (EngagementEventType type : values()) {
      if (type.code.equals(code)) {
        return type;
      }
    }
    throw new IllegalArgumentException("Unknown engagement_event_type code: " + code);
  }
}
