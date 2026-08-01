package dev.civicpulse.assistant.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.civicpulse.assistant.application.port.in.GenerateSocialVariantsUseCase;
import dev.civicpulse.assistant.application.port.out.GeminiGateway;
import dev.civicpulse.assistant.domain.exception.AiUnavailableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/** Turns one post into per-network variants with a single real Gemini call — the AI is asked to
 * follow each network's own real-world conventions (X's hard character limit, Instagram's
 * hashtag/emoji style, LinkedIn's formal register, a plain-language summary for accessibility)
 * and to reply with structured JSON, same contract style as VerifyDocumentAmountService. */
@Service
public class GenerateSocialVariantsService implements GenerateSocialVariantsUseCase {

  private static final Logger log = LoggerFactory.getLogger(GenerateSocialVariantsService.class);

  private static final String SYSTEM_INSTRUCTION =
      """
      You are CivicPulse's social-media assistant for elected officials and party communications \
      teams. You are given the text of one post a politician already published on the platform's \
      own feed. Your job is to rewrite it into ready-to-publish versions for other networks, and \
      a plain-language summary — never inventing facts not present in the original text.

      Rules for each variant:
      - "instagram": Warm, engaging opening line, short paragraphs, 2-5 relevant hashtags at the \
        end, light emoji use where natural. Up to ~2200 characters, but prefer concise.
      - "facebook": Conversational and informative, can include a little more context/background \
        than the original, minimal hashtags (0-2). Keep it to one short paragraph (roughly 400-600 \
        characters) — informative, not exhaustive.
      - "x": Must fit in 280 characters INCLUDING any hashtags — this is a hard platform limit, \
        never exceed it. Punchy, 1-2 hashtags at most.
      - "linkedin": Professional register, no emoji, can reference the broader policy context, \
        ends with a clear takeaway. Keep it to 2 short paragraphs at most (roughly 500-700 \
        characters).
      - "simpleSummary": The same announcement rewritten in plain, simple language a citizen with \
        no political background could understand in one read — short sentences, no jargon, no \
        hashtags, 2-4 sentences.

      Always reply in the same language as the original post text.

      Reply with ONLY a single JSON object, no markdown fences, no extra text, in exactly this \
      shape: {"instagram": "...", "facebook": "...", "x": "...", "linkedin": "...", \
      "simpleSummary": "..."}
      """;

  private final GeminiGateway geminiGateway;
  private final ObjectMapper objectMapper;

  public GenerateSocialVariantsService(GeminiGateway geminiGateway, ObjectMapper objectMapper) {
    this.geminiGateway = geminiGateway;
    this.objectMapper = objectMapper;
  }

  @Override
  public Result generate(String postText) {
    String rawAnswer = geminiGateway.generateAnswer(SYSTEM_INSTRUCTION, "Original post:\n" + postText);
    return parse(rawAnswer);
  }

  private Result parse(String rawAnswer) {
    String json = stripMarkdownFences(rawAnswer);
    try {
      return objectMapper.readValue(json, Result.class);
    } catch (Exception e) {
      log.warn("Could not parse Gemini's social-variants answer as JSON: {}", rawAnswer, e);
      throw new AiUnavailableException("The AI assistant could not generate social media versions — please try again.");
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
}
