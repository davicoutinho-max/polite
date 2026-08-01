package dev.civicpulse.assistant.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.civicpulse.assistant.application.port.in.VerifyDocumentAmountUseCase;
import dev.civicpulse.assistant.application.port.out.DocumentFetchGateway;
import dev.civicpulse.assistant.application.port.out.GeminiGateway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/** Reads an attached accountability document (receipt/invoice/statement) with Gemini's document
 * understanding and checks whether it actually supports the amount a politician declared — the
 * real check backing legislative-service's accountability-disclosure workflow. A document the
 * model can't parse, or one whose total doesn't match, is a normal "no match" result (see
 * VerifyDocumentAmountUseCase's javadoc); only the AI/document being unreachable is exceptional. */
@Service
public class VerifyDocumentAmountService implements VerifyDocumentAmountUseCase {

  private static final Logger log = LoggerFactory.getLogger(VerifyDocumentAmountService.class);

  private static final String SYSTEM_INSTRUCTION =
      """
      You are CivicPulse's accountability-verification assistant. You are given one attached \
      document (a receipt, invoice, bank statement, or similar proof of an expense) and a \
      monetary amount a politician has declared for a specific public-money category. Your ONLY \
      job is to read the document and judge whether it genuinely supports that declared amount.

      Rules:
      1. Look for the document's total amount (in Brazilian reais, R$). Small differences due to \
         taxes, fees or rounding (up to about 5%) still count as a match.
      2. If the document is illegible, unrelated to an expense/receipt, or you cannot find a \
         total amount at all, treat that as NOT matching and say so plainly in the feedback.
      3. Reply with ONLY a single JSON object, no markdown fences, no extra text, in exactly this \
         shape: {"extractedAmountCents": <integer amount in cents found in the document, or null \
         if none could be found>, "matches": <true or false>, "feedback": "<one short sentence in \
         Portuguese explaining your judgment to the politician>"}
      """;

  private final GeminiGateway geminiGateway;
  private final DocumentFetchGateway documentFetchGateway;
  private final ObjectMapper objectMapper;

  public VerifyDocumentAmountService(GeminiGateway geminiGateway, DocumentFetchGateway documentFetchGateway, ObjectMapper objectMapper) {
    this.geminiGateway = geminiGateway;
    this.documentFetchGateway = documentFetchGateway;
    this.objectMapper = objectMapper;
  }

  @Override
  public Result verify(String documentUrl, String categoryLabel, long declaredAmountCents) {
    var document = documentFetchGateway.fetch(documentUrl);
    String declaredAmountReais = String.format("%.2f", declaredAmountCents / 100.0);
    String prompt =
        "Public-money category: " + categoryLabel + "\nDeclared amount: R$ " + declaredAmountReais + "\n\n"
            + "Read the attached document and judge whether it supports this declared amount.";
    String rawAnswer = geminiGateway.generateAnswerWithDocument(SYSTEM_INSTRUCTION, prompt, document.bytes(), document.mimeType());
    return parse(rawAnswer);
  }

  private Result parse(String rawAnswer) {
    String json = stripMarkdownFences(rawAnswer);
    try {
      GeminiVerdict verdict = objectMapper.readValue(json, GeminiVerdict.class);
      return new Result(verdict.matches(), verdict.extractedAmountCents(), verdict.feedback());
    } catch (Exception e) {
      log.warn("Could not parse Gemini's document-verification answer as JSON: {}", rawAnswer, e);
      return new Result(false, null, "The AI reviewer could not produce a clear verdict for this document — please try again.");
    }
  }

  private static String stripMarkdownFences(String text) {
    String trimmed = text.trim();
    if (trimmed.startsWith("```")) {
      trimmed = trimmed.replaceFirst("^```[a-zA-Z]*\\n?", "");
      int lastFence = trimmed.lastIndexOf("```");
      if (lastFence >= 0) {
        trimmed = trimmed.substring(0, lastFence);
      }
    }
    return trimmed.trim();
  }

  private record GeminiVerdict(Long extractedAmountCents, boolean matches, String feedback) {}
}
