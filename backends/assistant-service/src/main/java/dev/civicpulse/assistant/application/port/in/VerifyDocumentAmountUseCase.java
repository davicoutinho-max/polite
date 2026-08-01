package dev.civicpulse.assistant.application.port.in;

public interface VerifyDocumentAmountUseCase {

  /** Downloads the document at {@code documentUrl}, asks Gemini to read it, and checks whether
   * its total supports {@code declaredAmountCents} for the given category. Always returns a
   * scored {@link Result} — a document the model genuinely can't read or that doesn't support the
   * amount is a normal "no match" result, not an exception (see GeminiGateway's contract for what
   * IS exceptional: the AI being unreachable). */
  Result verify(String documentUrl, String categoryLabel, long declaredAmountCents);

  record Result(boolean matches, Long extractedAmountCents, String feedback) {}
}
