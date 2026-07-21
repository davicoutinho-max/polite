package dev.civicpulse.platformconfig.adapter.in.web.dto;

import dev.civicpulse.platformconfig.domain.model.TranslationKey;
import java.util.UUID;

public record TranslationKeyResponse(UUID id, String key) {

  public static TranslationKeyResponse from(TranslationKey translationKey) {
    return new TranslationKeyResponse(translationKey.id(), translationKey.key());
  }
}
