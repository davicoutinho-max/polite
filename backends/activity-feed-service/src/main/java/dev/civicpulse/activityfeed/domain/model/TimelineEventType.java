package dev.civicpulse.activityfeed.domain.model;

/** Mirrors {@code timeline_event_type_options}. Only types with a real upstream producer are
 * listed — the frontend mock's 'honor'/'event'/'accounts' types have no real event anywhere in
 * the platform and are intentionally dropped (see schema.sql's table comment). */
public enum TimelineEventType {
  VOTE("vote"),
  PROJECT("project"),
  PEC("pec"),
  CPI("cpi"),
  STATUS_CHANGE("status_change"),
  COMMITTEE("committee"),
  VIDEO("video"),
  POST("post"),
  PARTY_CHANGE("party_change"),
  CAMPAIGN("campaign");

  private final String code;

  TimelineEventType(String code) {
    this.code = code;
  }

  public String code() {
    return code;
  }

  public static TimelineEventType fromCode(String code) {
    for (TimelineEventType type : values()) {
      if (type.code.equals(code)) {
        return type;
      }
    }
    throw new IllegalArgumentException("Unknown timeline_event_type code: " + code);
  }

  public static TimelineEventType fromLegislativeItemCategory(String category) {
    return switch (category) {
      case "project" -> PROJECT;
      case "pec" -> PEC;
      case "cpi" -> CPI;
      default -> throw new IllegalArgumentException("Unknown legislative item category: " + category);
    };
  }
}
