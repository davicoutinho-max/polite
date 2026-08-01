package dev.civicpulse.assistant.adapter.in.web;

import dev.civicpulse.assistant.adapter.in.web.dto.VerifyDocumentRequest;
import dev.civicpulse.assistant.adapter.in.web.dto.VerifyDocumentResponse;
import dev.civicpulse.assistant.application.port.in.VerifyDocumentAmountUseCase;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Internal-only — deliberately mapped at bare {@code /document-verifications}, not under
 * {@code /assistant/**} like every citizen-facing controller in this service, so it falls
 * outside every existing Gateway route's match (see gateway-service's RouteConfig: the
 * {@code /api/assistant/**} route only strips the literal "api" segment, so it can only ever
 * reach paths actually rooted at {@code /assistant/...} on this service — this path is
 * structurally unreachable through it). Only legislative-service calls this, directly. */
@RestController
@RequestMapping("/document-verifications")
public class DocumentVerificationController {

  private final VerifyDocumentAmountUseCase verifyDocumentAmountUseCase;

  public DocumentVerificationController(VerifyDocumentAmountUseCase verifyDocumentAmountUseCase) {
    this.verifyDocumentAmountUseCase = verifyDocumentAmountUseCase;
  }

  @PostMapping
  public VerifyDocumentResponse verify(@Valid @RequestBody VerifyDocumentRequest request) {
    var result = verifyDocumentAmountUseCase.verify(request.documentUrl(), request.categoryLabel(), request.declaredAmountCents());
    return VerifyDocumentResponse.from(result);
  }
}
