package dev.civicpulse.assistant.application.port.out;

public interface GeminiGateway {

  /** Calls the model with a system instruction (scope/guardrails) plus the user-facing prompt.
   * Throws {@link dev.civicpulse.assistant.domain.exception.AiUnavailableException} if the API
   * key isn't configured or the upstream call fails — callers must not treat that as a 500, see
   * GlobalExceptionHandler. */
  String generateAnswer(String systemInstruction, String userPrompt);

  /** Same contract as {@link #generateAnswer}, but the prompt is grounded in an actual attached
   * document (image or PDF) via Gemini's inline document understanding — used by
   * legislative-service's accountability-disclosure verification to actually read a receipt/
   * invoice rather than trust the declared amount blindly. */
  String generateAnswerWithDocument(String systemInstruction, String userPrompt, byte[] documentBytes, String mimeType);
}
