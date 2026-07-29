package dev.civicpulse.assistant.application.port.in;

public interface AskBillQuestionUseCase {

  /** {@code billIdentification}/{@code billSummary} are supplied by the caller (the frontend
   * already has them loaded from Câmara/Senado open data) rather than re-fetched here — this
   * service only ever needs them as grounding context for one question, not as data to own. */
  String ask(String billIdentification, String billSummary, String question);
}
