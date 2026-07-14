package dev.civicpulse.feedcontent.domain.model;

/** Mirrors {@code post_visibility_options}. */
public enum PostVisibility {
  PUBLIC("public"),
  PRIVATE("private");

  private final String code;

  PostVisibility(String code) {
    this.code = code;
  }

  public String code() {
    return code;
  }

  public static PostVisibility fromCode(String code) {
    for (PostVisibility visibility : values()) {
      if (visibility.code.equals(code)) {
        return visibility;
      }
    }
    throw new IllegalArgumentException("Unknown post_visibility code: " + code);
  }
}
