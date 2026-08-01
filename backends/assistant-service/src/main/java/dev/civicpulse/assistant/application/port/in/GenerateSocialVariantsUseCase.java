package dev.civicpulse.assistant.application.port.in;

public interface GenerateSocialVariantsUseCase {

  /** Turns one post's raw text into ready-to-use variants tailored to each network's own tone,
   * length and conventions, plus a plain-language summary — so a politician (or their comms
   * team) can write one update and get every channel's version from it instead of retyping the
   * same announcement four times by hand. */
  Result generate(String postText);

  record Result(String instagram, String facebook, String x, String linkedin, String simpleSummary) {}
}
