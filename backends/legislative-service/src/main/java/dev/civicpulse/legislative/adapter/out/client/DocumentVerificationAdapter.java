package dev.civicpulse.legislative.adapter.out.client;

import dev.civicpulse.legislative.application.port.out.DocumentVerificationGateway;
import dev.civicpulse.legislative.domain.exception.DocumentVerificationException;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/** Calls assistant-service directly (internal-only endpoint, never Gateway-routed — same
 * "structurally unreachable" convention as every other internal-only endpoint in this codebase,
 * see gateway-service's RouteConfig javadoc), which does the actual document download + Gemini
 * call. This adapter is a thin anti-corruption layer, same pattern as elections-service's
 * PoliticianDirectoryAdapter. */
@Component
class DocumentVerificationAdapter implements DocumentVerificationGateway {

  private final RestClient restClient;

  DocumentVerificationAdapter(RestClient.Builder restClientBuilder, AssistantServiceProperties properties) {
    this.restClient = restClientBuilder.baseUrl(properties.baseUrl()).build();
  }

  @Override
  public Result verify(String documentUrl, String categoryLabel, long declaredAmountCents) {
    try {
      VerifyDocumentResponse response =
          restClient
              .post()
              .uri("/document-verifications")
              .body(new VerifyDocumentRequest(documentUrl, categoryLabel, declaredAmountCents))
              .retrieve()
              .body(VerifyDocumentResponse.class);
      if (response == null) {
        throw new DocumentVerificationException("assistant-service returned an empty document-verification response", null);
      }
      return new Result(response.matches(), response.extractedAmountCents(), response.feedback());
    } catch (RestClientException e) {
      throw new DocumentVerificationException("assistant-service was unreachable while verifying the attached document", e);
    }
  }

  private record VerifyDocumentRequest(String documentUrl, String categoryLabel, long declaredAmountCents) {}

  private record VerifyDocumentResponse(boolean matches, Long extractedAmountCents, String feedback) {}
}
