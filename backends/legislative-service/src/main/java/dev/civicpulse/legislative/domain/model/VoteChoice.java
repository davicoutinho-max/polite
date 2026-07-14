package dev.civicpulse.legislative.domain.model;

/** Mirrors {@code vote_choice_options}. */
public enum VoteChoice {
  YES("yes"),
  NO("no"),
  ABSTAIN("abstain"),
  ABSENT("absent");

  private final String code;

  VoteChoice(String code) {
    this.code = code;
  }

  public String code() {
    return code;
  }

  public static VoteChoice fromCode(String code) {
    for (VoteChoice choice : values()) {
      if (choice.code.equals(code)) {
        return choice;
      }
    }
    throw new IllegalArgumentException("Unknown vote_choice code: " + code);
  }
}
