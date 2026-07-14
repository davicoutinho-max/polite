package dev.civicpulse.notification.domain.model;

/** Mirrors {@code notification_category_options}. */
public enum NotificationCategory {
  PROJECT("project"),
  PEC("pec"),
  PARTY("party"),
  VOTE("vote"),
  CPI("cpi"),
  CAMPAIGN("campaign");

  private final String code;

  NotificationCategory(String code) {
    this.code = code;
  }

  public String code() {
    return code;
  }

  public static NotificationCategory fromCode(String code) {
    for (NotificationCategory category : values()) {
      if (category.code.equals(code)) {
        return category;
      }
    }
    throw new IllegalArgumentException("Unknown notification_category code: " + code);
  }
}
