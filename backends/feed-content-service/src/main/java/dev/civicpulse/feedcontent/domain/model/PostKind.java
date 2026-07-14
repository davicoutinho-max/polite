package dev.civicpulse.feedcontent.domain.model;

/** Mirrors {@code post_kind_options}. */
public enum PostKind {
  TEXT("text"),
  AGENDA("agenda"),
  LIVE("live");

  private final String code;

  PostKind(String code) {
    this.code = code;
  }

  public String code() {
    return code;
  }

  public static PostKind fromCode(String code) {
    for (PostKind kind : values()) {
      if (kind.code.equals(code)) {
        return kind;
      }
    }
    throw new IllegalArgumentException("Unknown post_kind code: " + code);
  }
}
