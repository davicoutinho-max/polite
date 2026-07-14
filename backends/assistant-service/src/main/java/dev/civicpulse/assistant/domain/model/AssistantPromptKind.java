package dev.civicpulse.assistant.domain.model;

/** Mirrors {@code assistant_prompt_kind_options}. */
public enum AssistantPromptKind {
  SUMMARY("summary"),
  PLAIN("plain"),
  IMPACT("impact");

  private final String code;

  AssistantPromptKind(String code) {
    this.code = code;
  }

  public String code() {
    return code;
  }

  public static AssistantPromptKind fromCode(String code) {
    for (AssistantPromptKind kind : values()) {
      if (kind.code.equals(code)) {
        return kind;
      }
    }
    throw new IllegalArgumentException("Unknown assistant_prompt_kind code: " + code);
  }
}
