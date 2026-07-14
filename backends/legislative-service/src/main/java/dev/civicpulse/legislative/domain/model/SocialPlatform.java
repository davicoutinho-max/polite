package dev.civicpulse.legislative.domain.model;

/** Mirrors {@code social_platform_options}. */
public enum SocialPlatform {
  WEBSITE("website"),
  INSTAGRAM("instagram"),
  X("x"),
  FACEBOOK("facebook"),
  YOUTUBE("youtube"),
  LINKEDIN("linkedin"),
  TIKTOK("tiktok");

  private final String code;

  SocialPlatform(String code) {
    this.code = code;
  }

  public String code() {
    return code;
  }

  public static SocialPlatform fromCode(String code) {
    for (SocialPlatform platform : values()) {
      if (platform.code.equals(code)) {
        return platform;
      }
    }
    throw new IllegalArgumentException("Unknown social_platform code: " + code);
  }
}
