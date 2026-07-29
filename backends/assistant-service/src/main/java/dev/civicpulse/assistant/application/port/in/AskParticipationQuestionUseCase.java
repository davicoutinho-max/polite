package dev.civicpulse.assistant.application.port.in;

public interface AskParticipationQuestionUseCase {

  /** {@code itemType}/{@code title}/{@code description} are supplied by the caller (the frontend
   * already has the petition/consultation/survey loaded) rather than re-fetched here — this
   * service only ever needs them as grounding context for one question, not as data to own. */
  String ask(String itemType, String title, String description, String question);
}
