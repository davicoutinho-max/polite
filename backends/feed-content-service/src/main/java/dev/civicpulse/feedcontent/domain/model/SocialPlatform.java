package dev.civicpulse.feedcontent.domain.model;

/** Mirrors {@code social_platform_options}. Facebook and Instagram are both reachable through
 * the same Meta developer app/OAuth flow (Instagram posting rides on a Facebook Page's linked
 * Instagram Business account) — X is a wholly separate app/flow. */
public enum SocialPlatform {
  FACEBOOK("facebook"),
  INSTAGRAM("instagram"),
  X("x");

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
