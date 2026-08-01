package dev.civicpulse.assistant.adapter.in.web.dto;

import dev.civicpulse.assistant.application.port.in.VerifyDocumentAmountUseCase.Result;

public record VerifyDocumentResponse(boolean matches, Long extractedAmountCents, String feedback) {

  public static VerifyDocumentResponse from(Result result) {
    return new VerifyDocumentResponse(result.matches(), result.extractedAmountCents(), result.feedback());
  }
}
