package dev.civicpulse.assistant.adapter.out.client;

import com.fasterxml.jackson.annotation.JsonInclude;
import dev.civicpulse.assistant.application.port.out.GeminiGateway;
import dev.civicpulse.assistant.domain.exception.AiUnavailableException;
import java.util.Base64;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/** Google's Generative Language API (generativelanguage.googleapis.com) — the real model backing
 * "Ask AI". {@code GEMINI_API_KEY} is read from the environment (see GeminiProperties' javadoc);
 * with no key configured this always fails closed (never falls back to the old templated
 * answers), so a missing key is loud and obvious in local dev rather than silently degrading. */
@Component
class GeminiApiClient implements GeminiGateway {

  private static final Logger log = LoggerFactory.getLogger(GeminiApiClient.class);

  // Conservative caps: this endpoint is reachable by visitors with no auth, so the request/response
  // size itself is the guardrail against runaway API cost from a single abusive caller — the
  // system instruction is the guardrail against off-topic/malicious *content*.
  private static final double TEMPERATURE = 0.3;
  // 2500 rather than a tighter per-answer budget because this now also backs multi-field JSON
  // answers (e.g. GenerateSocialVariantsService's five variants in one call, which was observed
  // truncating mid-string at 1200 — five real social-media-length texts plus JSON overhead adds
  // up fast) — a single short Q&A answer still finishes well under this, so raising it doesn't
  // make normal answers longer.
  private static final int MAX_OUTPUT_TOKENS = 2500;

  private final RestClient restClient;
  private final GeminiProperties properties;

  GeminiApiClient(RestClient.Builder restClientBuilder, GeminiProperties properties) {
    this.restClient = restClientBuilder.baseUrl(properties.baseUrl()).build();
    this.properties = properties;
  }

  @Override
  public String generateAnswer(String systemInstruction, String userPrompt) {
    return call(systemInstruction, List.of(Part.text(userPrompt)));
  }

  @Override
  public String generateAnswerWithDocument(String systemInstruction, String userPrompt, byte[] documentBytes, String mimeType) {
    String base64Data = Base64.getEncoder().encodeToString(documentBytes);
    return call(systemInstruction, List.of(Part.text(userPrompt), Part.inlineData(mimeType, base64Data)));
  }

  private String call(String systemInstruction, List<Part> userParts) {
    if (properties.apiKey() == null || properties.apiKey().isBlank()) {
      throw new AiUnavailableException(
          "GEMINI_API_KEY is not configured — copy assistant-service/.env.example to .env and set a real key");
    }
    GenerateContentRequest request =
        new GenerateContentRequest(
            new SystemInstruction(List.of(Part.text(systemInstruction))),
            List.of(new Content("user", userParts)),
            new GenerationConfig(TEMPERATURE, MAX_OUTPUT_TOKENS),
            List.of(
                new SafetySetting("HARM_CATEGORY_HARASSMENT", "BLOCK_MEDIUM_AND_ABOVE"),
                new SafetySetting("HARM_CATEGORY_HATE_SPEECH", "BLOCK_MEDIUM_AND_ABOVE"),
                new SafetySetting("HARM_CATEGORY_SEXUALLY_EXPLICIT", "BLOCK_MEDIUM_AND_ABOVE"),
                new SafetySetting("HARM_CATEGORY_DANGEROUS_CONTENT", "BLOCK_MEDIUM_AND_ABOVE")));
    try {
      GenerateContentResponse response =
          restClient
              .post()
              .uri("/v1beta/models/{model}:generateContent?key={key}", properties.model(), properties.apiKey())
              .body(request)
              .retrieve()
              .body(GenerateContentResponse.class);
      return extractText(response);
    } catch (RestClientException e) {
      log.warn("Gemini call failed: {}", e.getMessage());
      throw new AiUnavailableException("The AI assistant is temporarily unavailable — please try again shortly.", e);
    }
  }

  private static String extractText(GenerateContentResponse response) {
    if (response == null || response.candidates() == null || response.candidates().isEmpty()) {
      throw new AiUnavailableException("The AI assistant returned no answer — please rephrase your question.");
    }
    Candidate candidate = response.candidates().get(0);
    if (candidate.content() == null || candidate.content().parts() == null || candidate.content().parts().isEmpty()) {
      // A finishReason of SAFETY/RECITATION with no parts means the model itself refused —
      // treated the same as "no answer" rather than exposing the raw finishReason to the caller.
      throw new AiUnavailableException("The AI assistant couldn't answer that question — please ask something about this bill.");
    }
    return candidate.content().parts().get(0).text();
  }

  private record GenerateContentRequest(
      SystemInstruction systemInstruction, List<Content> contents, GenerationConfig generationConfig, List<SafetySetting> safetySettings) {}

  private record SystemInstruction(List<Part> parts) {}

  private record Content(String role, List<Part> parts) {}

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private record Part(String text, InlineData inlineData) {
    static Part text(String text) {
      return new Part(text, null);
    }

    static Part inlineData(String mimeType, String base64Data) {
      return new Part(null, new InlineData(mimeType, base64Data));
    }
  }

  private record InlineData(String mimeType, String data) {}

  private record GenerationConfig(double temperature, int maxOutputTokens) {}

  private record SafetySetting(String category, String threshold) {}

  private record GenerateContentResponse(List<Candidate> candidates) {}

  private record Candidate(Content content, String finishReason) {}
}
