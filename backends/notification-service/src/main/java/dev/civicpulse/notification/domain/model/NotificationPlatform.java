package dev.civicpulse.notification.domain.model;

/** Mirrors {@code platform_options}. */
public enum NotificationPlatform {
  IOS("ios"),
  ANDROID("android"),
  WEB("web");

  private final String code;

  NotificationPlatform(String code) {
    this.code = code;
  }

  public String code() {
    return code;
  }

  public static NotificationPlatform fromCode(String code) {
    for (NotificationPlatform platform : values()) {
      if (platform.code.equals(code)) {
        return platform;
      }
    }
    throw new IllegalArgumentException("Unknown platform code: " + code);
  }
}
